package v2

import v2.Compass16.*
import java.io.Serializable

/**
 * Reference to the tile grid, (x,y) refers to top-left corner.
 */
data class Position(val x: Int, val y: Int) : Serializable

enum class Compass16 {
    NNE, ENE, ESE, SSE, SSW, WSW, WNW, NNW
}

class BoardInformationBuilder {

    private val map: MutableMap<Position, Information> = HashMap()

    fun put(p: Pair<Position, Information>) {
        map[p.first] = p.second
    }

    fun build() = BoardInformation(map)
}

interface Information

data class Connection(val from: Compass16, val to: Compass16)

fun Pair<Compass16, Compass16>.toConnection(): Connection = Connection(first, second)

data class ConnectorInformation(val connections: Set<Connection>) : Information

fun BoardInformationBuilder.connector(x: Int, y: Int, vararg connections: Pair<Compass16, Compass16>) =
        put(Position(x, y) to ConnectorInformation(connections.map { it.toConnection() }.toSet()))

data class InputInformation(val state: Boolean, val outputs: Set<Compass16>) : Information

fun BoardInformationBuilder.input(x: Int, y: Int, state: Boolean, vararg outputs: Compass16) =
        put(Position(x, y) to InputInformation(state, setOf(*outputs)))

data class OutputInformation(val expectedState: Boolean, val inputs: Set<Compass16>) : Information

fun BoardInformationBuilder.output(x: Int, y: Int, expectedState: Boolean, vararg inputs: Compass16) =
        put(Position(x, y) to OutputInformation(expectedState, setOf(*inputs)))

data class UnaryGateInformation(
        val functionDescription: String,
        val input: Compass16,
        val outputs: Set<Compass16>
) : Information

fun BoardInformationBuilder.unary(
        x: Int,
        y: Int,
        description: String,
        input: Compass16,
        vararg outputs: Compass16
) = put(Position(x, y) to UnaryGateInformation(description, input, setOf(*outputs)))

data class BinaryGateInformation(
        val functionDescription: String,
        val input1: Compass16,
        val input2: Compass16,
        val outputs: Set<Compass16>
) : Information

data class BoardInformation(val informationMap: Map<Position, Information>) : Information

fun BoardInformationBuilder.binary(
        x: Int,
        y: Int,
        description: String,
        input1: Compass16,
        input2: Compass16,
        vararg outputs: Compass16
) = put(Position(x, y) to BinaryGateInformation(description, input1, input2, setOf(*outputs)))

inline fun board(init: BoardInformationBuilder.() -> Unit): BoardInformation = BoardInformationBuilder().also(init).build()

val defaultBoardInformation: BoardInformation = board {
    // ROW 0
    input(0, 0, true, SSW, SSE)
    input(1, 0, true, SSE)
    input(2, 0, true, SSE)
    input(3, 0, false, SSW)
    input(4, 0, true, SSW)
    // ROW 1
    connector(0, 1, NNW to SSW, NNE to ENE)
    binary(1, 1, "NAND", WNW, NNE, SSW, SSE)
    connector(2, 1, NNE to ENE)
    connector(3, 1, WNW to SSW, NNW to SSE)
    connector(4, 1, NNW to SSE)
    // ROW 2
    connector(0, 2, NNW to SSW, ENE to SSE)
    connector(1, 2, NNW to WNW, NNE to ENE)
    connector(2, 2, WNW to SSW)
    binary(3, 2, "OR", NNW, NNE, SSW, ESE)
    connector(4, 2, WSW to SSW, NNE to SSE)
    // ROW 3
    binary(0, 3, "XNOR", NNW, NNE, SSW, SSE)
    connector(1, 3)
    binary(2, 3, "OR", NNW, ENE, SSW, SSE)
    connector(3, 3, NNW to WNW, ESE to SSE)
    binary(4, 3, "NOR", NNW, NNE, WSW, SSE)
    // ROW 4
    connector(0, 4, NNW to SSW, NNE to ENE, ESE to SSE)
    connector(1, 4, WNW to SSE, ENE to WSW)
    connector(2, 4, NNW to WNW, NNE to ENE, ESE to SSW)
    binary(3, 4, "XOR", WNW, NNE, WSW, SSE)
    connector(4, 4, NNE to SSE)
    // ROW 5
    binary(0, 5, "XNOR", NNW, NNE, SSW, SSE)
    connector(1, 5, NNE to SSE)
    binary(2, 5, "AND", NNW, ENE, SSW, ESE)
    connector(3, 5, NNE to WNW, WSW to SSW)
    connector(4, 5, NNE to SSE)
    // ROW 6
    connector(0, 6, NNW to SSE, NNE to ENE)
    connector(1, 6, WNW to SSE, NNE to ENE)
    connector(2, 6, WNW to SSE, NNW to ENE, ESE to SSW)
    connector(3, 6, NNW to ENE, WNW to SSW, ESE to WSW)
    binary(4, 6, "NAND", WNW, NNE, WSW, SSE)
    // ROW 7
    unary(0, 7, "NOT", NNE, SSW)
    connector(1, 7, NNE to SSW)
    binary(2, 7, "NOR", NNW, NNE, SSE)
    connector(3, 7, NNW to SSE)
    connector(4, 7, NNE to SSE)
    // ROW 8
    output(0, 8, true, NNW)
    output(1, 8, false, NNW)
    output(2, 8, false, NNE)
    output(3, 8, true, NNE)
    output(4, 8, true, NNE)
}

