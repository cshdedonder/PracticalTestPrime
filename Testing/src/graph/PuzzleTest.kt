package graph

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import graph.Compass16.*
import graph.LogicGate.*
import java.io.*
import kotlin.system.measureTimeMillis
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

fun input(x: Int, y: Int, state: Boolean, vararg outputs: Compass16) = Position(x, y) to InputTileGraph(state, outputs.toSet())
fun output(x: Int, y: Int, expectedState: Boolean, vararg inputs: Compass16) = Position(x, y) to OutputTileGraph(expectedState, inputs.toSet())
fun connector(vararg connections: Pair<Compass16, Compass16>) = ConnectorTileGraph(connections.toSet())
fun notgate(input: Compass16, vararg outputs: Compass16) = UnaryGatedTileGraph(NOTGateFunction, input, outputs.toSet())
enum class LogicGate {
    AND, OR, NOR, XOR, NAND, XNOR
}

fun binarygate(gate: LogicGate, input1: Compass16, input2: Compass16, vararg outputs: Compass16) = when (gate) {
    AND -> BinaryGatedTileGraph(ANDGateFunction, input1, input2, outputs.toSet())
    OR -> BinaryGatedTileGraph(ORGateFunction, input1, input2, outputs.toSet())
    NOR -> BinaryGatedTileGraph(NORGateFunction, input1, input2, outputs.toSet())
    XOR -> BinaryGatedTileGraph(XORGateFunction, input1, input2, outputs.toSet())
    NAND -> BinaryGatedTileGraph(NANDGateFunction, input1, input2, outputs.toSet())
    XNOR -> BinaryGatedTileGraph(XNORGateFunction, input1, input2, outputs.toSet())
}

val fixedTiles: Collection<Pair<Position, TileGraph>> = listOf(
        input(0, 0, true, SSW, SSE),
        input(1, 0, true, SSE),
        input(2, 0, true, SSE),
        input(3, 0, false, SSW),
        input(4, 0, true, SSW),
        output(0, 8, true, NNW),
        output(1, 8, false, NNW),
        output(2, 8, false, NNE),
        output(3, 8, true, NNE),
        output(4, 8, true, NNE)/*,
        Position(0, 2) to connector(NNW to SSW, ENE to SSE),
        Position(4, 6) to binarygate(NAND, WNW, NNE, WSW, SSE)*/
)

val freeTiles: List<TileGraph> = listOf(
        // ROW 1
        connector(NNW to SSW, NNE to ENE),
        binarygate(NAND, WNW, NNE, SSW, SSE),
        connector(NNE to ENE),
        connector(WNW to SSW, NNW to SSE),
        connector(NNW to SSE),
        // ROW 2
        connector(NNW to SSW, ENE to SSE),
        connector(NNW to WNW, NNE to ENE),
        connector(WNW to SSW),
        binarygate(OR, NNW, NNE, SSW, ESE),
        connector(WSW to SSW, NNE to SSE),
        // ROW 3
        binarygate(XNOR, NNW, NNE, SSW, SSE),
        connector(),
        binarygate(OR, NNW, ENE, SSW, SSE),
        connector(NNW to WNW, ESE to SSE),
        binarygate(NOR, NNW, NNE, WSW, SSE),
        // ROW 4
        connector(NNW to SSW, NNE to ENE, ESE to SSE),
        connector(WNW to SSE, ENE to WSW),
        connector(NNW to WNW, NNE to ENE, ESE to SSW),
        binarygate(XOR, WNW, NNE, WSW, SSE),
        connector(NNE to SSE),
        // ROW 5
        binarygate(XNOR, NNW, NNE, SSW, SSE),
        connector(NNE to SSE),
        binarygate(AND, NNW, ENE, SSW, ESE),
        connector(NNE to WNW, WSW to SSW),
        connector(NNE to SSE),
        // ROW 6
        connector(NNW to SSE, NNE to ENE),
        connector(WNW to SSE, NNE to ENE),
        connector(WNW to SSE, NNW to ENE, ESE to SSW),
        connector(NNW to ENE, WNW to SSW, ESE to WSW),
        binarygate(NAND, WNW, NNE, WSW, SSE),
        // ROW 7
        notgate(NNE, SSW),
        connector(NNE to SSW),
        binarygate(NOR, NNW, NNE, SSE),
        connector(NNW to SSE),
        connector(NNE to SSE)
)

@UseExperimental(ExperimentalTime::class)
fun testInitialConfiguration() {
    val board = PuzzleBoard(defaultConfiguration)
    fixedTiles.forEach { (p, t) ->
        board.addTile(p, t)
    }
    var position = Position(0, 1)
    val time = measureTimedValue {
        freeTiles.forEach {
            if (board.checkIfValid(position, it))
                board.addTile(position, it)
            else
                println("Invalid tile!")
            position = position.next(defaultConfiguration)!!
        }
    }
    println("Checked and added ${freeTiles.size} tiles in ${time.duration.inMilliseconds}ms.")
    println("Board size: ${board.size}")
    println("Board graph size: ${board.vertexSet().size}")
    println("Board graph edges size: ${board.edgeSet().size}")
    with(measureTimedValue {
        board.runSelfCheck()
    }) {
        println("Acyclic check: $value in ${duration.inMilliseconds}ms.")
    }
}

fun testRemoval() {
    val board = PuzzleBoard(defaultConfiguration)
    fixedTiles.forEach { (p, t) ->
        board.addTile(p, t)
    }
    val oldBoard = board.clone() as PuzzleBoard
    println("Initialized board:")
    println("Board size: ${board.size}")
    println("Board graph size: ${board.vertexSet().size}")
    println("Board graph edges size: ${board.edgeSet().size}")
    board.addTile(Position(0, 1), freeTiles[0])
    println("Board with 1 tile added:")
    println("Board size: ${board.size}")
    println("Board graph size: ${board.vertexSet().size}")
    println("Board graph edges size: ${board.edgeSet().size}")
    board.remove(Position(0, 1))
    println("Board with tile removed:")
    println("Board size: ${board.size}")
    println("Board graph size: ${board.vertexSet().size}")
    println("Board graph edges size: ${board.edgeSet().size}")
    println("\nRunning equals: ${oldBoard == board}")
}

fun testRemovalAll() {
    val board = PuzzleBoard(defaultConfiguration)
    fixedTiles.forEach { (p, t) ->
        board.addTile(p, t)
    }
    var position = Position(0, 1)
    freeTiles.forEach {
        if (board.checkIfValid(position, it))
            board.addTile(position, it)
        else
            println("Invalid tile!")
        position = position.next(defaultConfiguration)!!
    }
    println("Board build ...")
    val time: Long = measureTimeMillis {
        val p = Position(0, 0)
        board.remove(p)
        p.iterator(defaultConfiguration).forEachRemaining {
            board.remove(it)
        }
    }
    println("All tiles removed in ${time}ms.")
}

fun testPermutations() {
    val board = PuzzleBoard(defaultConfiguration)
    fixedTiles.forEach { (p, t) ->
        board.addTile(p, t)
    }
    val position = Position(0, 1)
    val collector: MutableSet<BoardInformation> = HashSet()
    val tiles = MaskingSet(freeTiles)
    testPermutation(board, position, tiles, collector, System.currentTimeMillis())
    println(collector.size)
    println("Writing set ...")
    ObjectOutputStream(FileOutputStream("set.ser")).writeObject(collector)
}

private fun Position.next() = next(defaultConfiguration)!!

var counter: Int = 1

val gson: Gson = GsonBuilder().setPrettyPrinting().create()

fun testPermutation(board: PuzzleBoard, position: Position, tiles: MaskingSet<TileGraph>, collector: MutableCollection<BoardInformation>, startTime: Long) {
    if (counter % 1000000 == 0)
        println("Running iteration ${counter / 1000000}M.")
    counter++
    if (tiles.isEmpty()) {
        if (board.runSelfCheck()) {
            val info = board.information
            if (collector.add(info)) {
                println("Found new valid board at #$counter after ${System.currentTimeMillis() - startTime}ms.")
                val writer = FileWriter("board-$counter.json")
                gson.toJson(info, writer)
                writer.flush()
                writer.close()
            }
        }
        return
    }
    for (tile: TileGraph in tiles) {
        if (board.checkIfValid(position, tile)) {
            board.addTile(position, tile)
            tiles.mask(tile)
            testPermutation(board, position.next(), tiles, collector, startTime)
            tiles.unmask(tile)
            board.remove(position)
        }
    }
}

val PuzzleBoard.information: BoardInformation
    get() = BoardInformation(tileMap.map { it.key to it.value.information }.toSet())

data class BoardInformation(val informationMap: Set<Pair<Position, Information>>) : Information, Serializable

fun testSerialize() {
    val board = PuzzleBoard(defaultConfiguration)
    fixedTiles.forEach { (p, t) ->
        board.addTile(p, t)
    }
    var position = Position(0, 1)
    freeTiles.forEach {
        if (board.checkIfValid(position, it))
            board.addTile(position, it)
        else
            println("Invalid tile!")
        position = position.next(defaultConfiguration)!!
    }
    ObjectOutputStream(FileOutputStream("test.ser")).writeObject(board.information)
    println(ObjectInputStream(FileInputStream("test.ser")).readObject() as BoardInformation)
}

fun main() {
    /*println("******* Initial Configuration")
     testInitialConfiguration()
     println("\n******* Removal")
     testRemoval()
     println("\n******* Removal All")
     testRemovalAll()*/
    testPermutations()
    //testSerialize()
}
