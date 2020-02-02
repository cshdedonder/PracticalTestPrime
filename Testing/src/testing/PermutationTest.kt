package testing

import testing.CompassRose16.*
import testing.LogicGate.*

val fixedTiles: Map<Position, Tile> = mapOf(
        inputTile(0, 0, SSW, ESE),
        inputTile(1, 0, SSE),
        inputTile(2, 0, SSE),
        inputTile(3, 0, SSW),
        inputTile(4, 0, SSW),
        outputTile(0, 8, NNW),
        outputTile(1, 8, NNW),
        outputTile(2, 8, NNE),
        outputTile(3, 8, NNE),
        outputTile(4, 8, NNE)
)

fun inputTile(x: Int, y: Int, vararg ports: CompassRose16): Pair<Position, Tile> = Position(x, y) to InputTile(*ports)
fun outputTile(x: Int, y: Int, vararg ports: CompassRose16): Pair<Position, Tile> = Position(x, y) to OutputTile(*ports)

fun connector(vararg connections: Pair<CompassRose16, CompassRose16>): ConnectorTile = ConnectorTile(connections.map { (s, e) -> Connection(s, e) })

enum class LogicGate {
    AND, NAND, OR, NOR, XOR, XNOR
}

fun bingate(g: LogicGate, vararg ports: CompassRose16): BinaryGatedTile = when (g) {
    AND -> bingateImpl(ports, ::AndGatedTile)
    NAND -> bingateImpl(ports, ::NandGatedTile)
    OR -> bingateImpl(ports, ::OrGatedTile)
    NOR -> bingateImpl(ports, ::NorGatedTile)
    XOR -> bingateImpl(ports, ::XorGatedTile)
    XNOR -> bingateImpl(ports, ::XnorGatedTile)
}

fun bingateImpl(ports: Array<out CompassRose16>, f: (CompassRose16, CompassRose16, Collection<CompassRose16>) -> BinaryGatedTile): BinaryGatedTile {
    return f(ports[0], ports[1], ports.drop(2))
}

val otherTiles: Set<Tile> = setOf(
        connector(NNW to SSW, NNE to ENE),
        bingate(AND, WNW, NNE, SSW, SSE) // TODO add all tiles
)

fun testPermutation() {}
