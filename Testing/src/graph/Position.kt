package graph

import graph.Compass4.*
import java.io.Serializable

data class Configuration(val width: Int, val height: Int) {
    fun positionIterator(start: Position = Position(0, 0)): Iterator<Position> = object : Iterator<Position> {

        private var nextPointer: Position? = start

        override fun hasNext(): Boolean = nextPointer != null

        override fun next(): Position {
            val p = nextPointer!!
            nextPointer = p.next(this@Configuration)
            return p
        }

    }

    operator fun contains(p: Position): Boolean = (p.x in 0 until width) and (p.y in 0 until height)
}

val defaultConfiguration = Configuration(5, 9)

data class Position(val x: Int, val y: Int) : Serializable {
    fun next(configuration: Configuration): Position? = when {
        (x in 0 until (configuration.width - 1)) -> Position(x + 1, y)
        (y in 0 until (configuration.height - 1)) -> Position(0, y + 1)
        else -> null
    }

    fun cardinalNeighbors(configuration: Configuration): Collection<Pair<Compass4, Position>> = Compass4.values().mapNotNull { c -> cardinalNeighbor(configuration, c)?.let { return@mapNotNull c to it } }

    fun cardinalNeighbor(configuration: Configuration, by: Compass4): Position? = when (by) {
        NORTH -> Position(x, y - 1).takeIf { it in configuration }
        EAST -> Position(x + 1, y).takeIf { it in configuration }
        SOUTH -> Position(x, y + 1).takeIf { it in configuration }
        WEST -> Position(x - 1, y).takeIf { it in configuration }
    }

    fun iterator(configuration: Configuration): Iterator<Position> = object : Iterator<Position> {

        private var currentPosition: Position = this@Position

        override fun hasNext(): Boolean = currentPosition.next(configuration) != null

        override fun next(): Position = currentPosition.next(configuration)!!.also { currentPosition = it }
    }
}