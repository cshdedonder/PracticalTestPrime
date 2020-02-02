package testing

import testing.CompassRose16.ESE
import testing.CompassRose16.NNW
import testing.CompassRose4.*
import testing.CompassRose8.*
import testing.TileBoard.Companion.tileBoard

data class Connection(val start: CompassRose16, val end: CompassRose16) : Iterable<CompassRose16> {
    override fun iterator(): Iterator<CompassRose16> = setOf(start, end).iterator()

    fun rotate(rotation: Rotation): Connection = Connection(start.rotate(rotation), end.rotate(rotation))
}

sealed class Tile {
    abstract val connectionPorts: Collection<CompassRose16>

    fun compatibleWith(other: Tile, by: CompassRose4): Boolean {
        return connectionPorts.filter { it.reduceToCompassRose4() == by } == other.connectionPorts.map { it.opposite }.filter { it.reduceToCompassRose4() == by }
    }
}

interface Symmetric {
    fun rotate(rotation: Rotation): Tile
}

class ConnectorTile(private val connections: Collection<Connection>) : Tile(), Symmetric {

    override val connectionPorts: Set<CompassRose16>
        get() = connections.flatten().toSet()

    override fun rotate(rotation: Rotation): ConnectorTile = ConnectorTile(connections.map { it.rotate(rotation) })

    class Builder {

        private val connectionList: MutableList<Connection> = ArrayList()

        operator fun Pair<CompassRose16, CompassRose16>.unaryPlus() {
            val (s, e) = this
            connectionList.add(Connection(s, e))
        }

        fun build() = ConnectorTile(connectionList)
    }
}

sealed class IOTile : Tile()

class InputTile(override val connectionPorts: Collection<CompassRose16>) : IOTile() {
    constructor(vararg ports: CompassRose16) : this(ports.toSet())
}

class OutputTile(override val connectionPorts: Collection<CompassRose16>) : IOTile() {
    constructor(vararg ports: CompassRose16) : this(ports.toSet())
}

sealed class GatedTile(protected val inputPorts: Collection<CompassRose16>, protected val outputPorts: Collection<CompassRose16>) : Tile() {

    override val connectionPorts: Collection<CompassRose16> by lazy {
        inputPorts.union(outputPorts)
    }

    abstract fun calculate(stateMap: Map<CompassRose16, Boolean>): Map<CompassRose16, Boolean>
}

sealed class UnaryGatedTile(protected val inputPort: CompassRose16, protected val outputPort: CompassRose16) : GatedTile(setOf(inputPort), setOf(outputPort))

class NotGatedTile(inputPort: CompassRose16, outputPort: CompassRose16) : UnaryGatedTile(inputPort, outputPort) {

    override fun calculate(stateMap: Map<CompassRose16, Boolean>): Map<CompassRose16, Boolean> {
        return mapOf(outputPort to !stateMap.getValue(inputPort))
    }
}

sealed class BinaryGatedTile(private val inputPort1: CompassRose16, private val inputPort2: CompassRose16, outputPorts: Collection<CompassRose16>) : GatedTile(setOf(inputPort1, inputPort2), outputPorts) {

    override fun calculate(stateMap: Map<CompassRose16, Boolean>): Map<CompassRose16, Boolean> {
        val value = stateMap.getValue(inputPort1) calc stateMap.getValue(inputPort2)
        return outputPorts.associateWith { value }
    }

    protected abstract infix fun Boolean.calc(other: Boolean): Boolean
}

class AndGatedTile(inputPort1: CompassRose16, inputPort2: CompassRose16, outputPorts: Collection<CompassRose16>) : BinaryGatedTile(inputPort1, inputPort2, outputPorts) {
    override fun Boolean.calc(other: Boolean): Boolean = this and other
}

class NandGatedTile(inputPort1: CompassRose16, inputPort2: CompassRose16, outputPorts: Collection<CompassRose16>) : BinaryGatedTile(inputPort1, inputPort2, outputPorts) {
    override fun Boolean.calc(other: Boolean): Boolean = !(this and other)
}

class OrGatedTile(inputPort1: CompassRose16, inputPort2: CompassRose16, outputPorts: Collection<CompassRose16>) : BinaryGatedTile(inputPort1, inputPort2, outputPorts) {
    override fun Boolean.calc(other: Boolean): Boolean = this or other
}

class XorGatedTile(inputPort1: CompassRose16, inputPort2: CompassRose16, outputPorts: Collection<CompassRose16>) : BinaryGatedTile(inputPort1, inputPort2, outputPorts) {
    override fun Boolean.calc(other: Boolean): Boolean = this xor other
}

class NorGatedTile(inputPort1: CompassRose16, inputPort2: CompassRose16, outputPorts: Collection<CompassRose16>) : BinaryGatedTile(inputPort1, inputPort2, outputPorts) {
    override fun Boolean.calc(other: Boolean): Boolean = !(this or other)
}

class XnorGatedTile(inputPort1: CompassRose16, inputPort2: CompassRose16, outputPorts: Collection<CompassRose16>) : BinaryGatedTile(inputPort1, inputPort2, outputPorts) {
    override fun Boolean.calc(other: Boolean): Boolean = !(this xor other)
}

class PositionContext(private val leftBound: Int, private val rightBound: Int, private val topBound: Int, private val bottomBound: Int) {

    operator fun contains(p: Position): Boolean = (p.indexX in leftBound..rightBound) and (p.indexY in topBound..bottomBound)
}

val defaultContext = PositionContext(0, 4, 0, 8)

data class Position(val indexX: Int, val indexY: Int, val context: PositionContext = defaultContext) {

    fun neighboursByCompassRose4(): Map<CompassRose4, Position> {
        return CompassRose4.values().mapNotNull {
            neighbourByCompassRose8(it)?.let { p ->
                return@mapNotNull it to p
            }
        }.toMap()
    }

    fun neighboursByCompassRose8(): Map<ICompassRose8, Position> {
        return CompassRose4.values().union<ICompassRose8>(CompassRose8.values().toList()).mapNotNull {
            neighbourByCompassRose8(it)?.let { p ->
                return@mapNotNull it to p
            }
        }.toMap()
    }

    private fun neighbourByCompassRose8(direction: ICompassRose8): Position? = when (direction) {
        N -> Position(indexX, indexY - 1, context).ifValid()
        NE -> Position(indexX + 1, indexY - 1, context).ifValid()
        E -> Position(indexX + 1, indexY, context).ifValid()
        SE -> Position(indexX + 1, indexY + 1, context).ifValid()
        S -> Position(indexX, indexY + 1, context).ifValid()
        SW -> Position(indexX - 1, indexY + 1, context).ifValid()
        W -> Position(indexX - 1, indexY, context).ifValid()
        NW -> Position(indexX - 1, indexY - 1, context).ifValid()
        else -> error("Exhaustive when: should not happen")
    }

    private fun ifValid(): Position? = takeIf { it in context }
}


class TileBoard {

    val tiles: MutableMap<Position, Tile> = HashMap()

    inline fun connector(position: Position, init: ConnectorTile.Builder.() -> Unit) {
        tiles += position to ConnectorTile.Builder().also(init).build()
    }

    inline fun connector(indexX: Int, indexY: Int, init: ConnectorTile.Builder.() -> Unit) = connector(Position(indexX, indexY), init)

    companion object {
        inline fun tileBoard(init: TileBoard.() -> Unit): TileBoard = TileBoard().also(init)
    }

    private operator fun Map<Position, Tile>.get(i: Int, j: Int): Tile? = get(Position(i, j))
}

fun main() {
    val board = tileBoard {
        connector(0, 0) {
            +(NNW to ESE)
        }
    }
}