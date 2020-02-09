package graph

import org.jgrapht.Graphs
import org.jgrapht.alg.cycle.CycleDetector
import org.jgrapht.graph.SimpleDirectedGraph
import org.jgrapht.traverse.DepthFirstIterator
import org.jgrapht.graph.DefaultEdge as Edge

class PuzzleBoard(private val configuration: Configuration) : SimpleDirectedGraph<Node, Edge>(Edge::class.java) {

    private val nodeMap: Map<NodePosition, Node> = HashMap<NodePosition, Node>().apply {
        configuration.positionIterator().forEachRemaining { p ->
            Compass16.values().forEach { c ->
                val node = Node()
                addVertex(node)
                put(NodePosition(p, c), node)
            }
        }
    }

    private val tileMap: MutableMap<Position, TileGraph> = HashMap()

    val size: Int
        get() = tileMap.size

    fun checkIfValid(position: Position, tile: TileGraph): Boolean {
        if (position in tileMap.keys)
            return false
        return position.cardinalNeighbors(configuration).filter { it.second in tileMap.keys }.all { checkIfValid(tile, tileMap.getValue(it.second), by = it.first) }
    }

    private fun checkIfValid(tile1: TileGraph, tile2: TileGraph, by: Compass4): Boolean {
        // FIXME check per node if incoming and outgoing number of edges is the same
        return Compass16.values().filter { it.compass4 == by }.map {
            it to tile1.degreeOf(tile1.externalNodeMap.getValue(it))
        }.toMap() == Compass16.values().filter { it.compass4 == by.rotate(RotationAngle.PI) }.map {
            it.opposite to tile2.degreeOf(tile2.externalNodeMap.getValue(it))
        }.toMap()
    }

    fun runSelfCheck(): Boolean {
        // CYCLE DETECTION
        val cycleDetection: CycleDetector<Node, Edge> = CycleDetector(this)
        if (cycleDetection.detectCycles())
            return false
        // ELECTRICAL PATH
        val node = vertexSet().find { it is InputNode }!!
        // FIXME
        val dfs = DepthFirstIterator(this, node)
        dfs.forEachRemaining {
            println(it)
        }
        return true
    }

    fun addTile(position: Position, tile: TileGraph) {
        if (position in tileMap.keys)
            throw IllegalArgumentException("$position is not clear.")
        tileMap[position] = tile
        Graphs.addGraph(this, tile)
        // FIXME edges need to be added with correct direction
        Compass16.values().map { NodePosition(position, it) }.forEach {
            if (tile.outDegreeOf(tile.externalNodeMap.getValue(it.compass16.opposite)) > 0)
                addEdge(nodeMap.getValue(it), tile.externalNodeMap.getValue(it.compass16.opposite))
            else
                addEdge(tile.externalNodeMap.getValue(it.compass16.opposite), nodeMap.getValue(it))
        }
    }

    private fun removeTile(tile: TileGraph): Boolean {
        if (tile !in tileMap.values)
            return false
        removeAllVertices(tile.vertexSet())
        return true
    }

    fun remove(position: Position): TileGraph? = tileMap.remove(position)?.also { removeTile(it) }
}

data class NodePosition(val position: Position, val compass16: Compass16)