package graph

import org.jgrapht.Graphs
import org.jgrapht.alg.cycle.CycleDetector
import org.jgrapht.graph.SimpleDirectedGraph
import org.jgrapht.graph.DefaultEdge as Edge

class PuzzleBoard(private val configuration: Configuration) : SimpleDirectedGraph<Node, Edge>(Edge::class.java) {

    private val nodeMap: Map<NodePosition, Node> = HashMap<NodePosition, Node>().apply {
        configuration.positionIterator().forEachRemaining { p ->
            Compass16.values().forEach { c ->
                val node = Node("Node@(${p.x}, ${p.y}, $c)")
                addVertex(node)
                put(NodePosition(p, c), node)
            }
        }
    }

    private val _tileMap: MutableMap<Position, TileGraph> = HashMap()
    val tileMap: Map<Position, TileGraph>
        get() = HashMap(_tileMap)

    val size: Int
        get() = _tileMap.size

    operator fun contains(position: Position): Boolean = position in _tileMap.keys

    fun checkIfValid(position: Position, tile: TileGraph): Boolean {
        if (position in _tileMap.keys)
            return false
        return position.cardinalNeighbors(configuration).filter { it.second in _tileMap.keys }.all { checkIfValid(tile, _tileMap.getValue(it.second), by = it.first) }
    }

    private fun checkIfValid(tile1: TileGraph, tile2: TileGraph, by: Compass4): Boolean {
        return Compass16.values().filter { it.compass4 == by }.all {
            val n1: Node = tile1.externalNodeMap.getValue(it)
            val n2: Node = tile2.externalNodeMap.getValue(it.opposite)
            tile1.inDegreeOf(n1) == tile2.outDegreeOf(n2) && tile1.outDegreeOf(n1) == tile2.inDegreeOf(n2)
        }
    }

    fun runSelfCheck(): Boolean {
        // CYCLE DETECTION
        val cycleDetection: CycleDetector<Node, Edge> = CycleDetector(this)
        if (cycleDetection.detectCycles())
            return false
        // ELECTRICAL PATH
        val flowDetector: FlowDetector = FlowDetector(this)
        if (!flowDetector.checkFlow(vertexSet().filterIsInstance<OutputNode>())) {
            return false
        }
        return true
    }

    fun addTile(position: Position, tile: TileGraph) {
        if (position in _tileMap.keys)
            throw IllegalArgumentException("$position is not clear.")
        _tileMap[position] = tile
        Graphs.addGraph(this, tile)
        tile.externalNodeMap.forEach { (c, n) ->
            val n1: Node = nodeMap.getValue(NodePosition(position, c))
            when {
                tile.inDegreeOf(n) == 1 -> {
                    addEdge(n, n1)
                    position.cardinalNeighbor(configuration, c.compass4)?.let {
                        if (it in _tileMap.keys) {
                            val n2: Node = nodeMap.getValue(NodePosition(it, c.opposite))
                            addEdge(n1, n2)
                            val n3: Node = _tileMap.getValue(it).externalNodeMap.getValue(c.opposite)
                            addEdge(n2, n3)
                        }
                    }
                }
                tile.outDegreeOf(n) == 1 -> {
                    addEdge(n1, n)
                    position.cardinalNeighbor(configuration, c.compass4)?.let {
                        if (it in _tileMap.keys) {
                            val n2: Node = nodeMap.getValue(NodePosition(it, c.opposite))
                            addEdge(n2, n1)
                            val n3: Node = _tileMap.getValue(it).externalNodeMap.getValue(c.opposite)
                            addEdge(n3, n2)
                        }
                    }
                }
            }
        }
    }

    fun remove(position: Position): Boolean {
        _tileMap[position]?.let {
            _tileMap.remove(position)
            removeAllVertices(it.vertexSet())
            Compass16.values().map { c -> nodeMap.getValue(NodePosition(position, c)) }.forEach { node ->
                removeAllEdges(edgesOf(node))
            }
            return true
        }
        return false
    }
}

data class NodePosition(val position: Position, val compass16: Compass16)