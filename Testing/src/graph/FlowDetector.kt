package graph

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultEdge

class FlowDetector(private val graph: Graph<Node, DefaultEdge>) {

    private val stateMap: MutableMap<Node, Boolean> = HashMap()

    fun checkFlow(startVertices: Collection<OutputNode>): Boolean = startVertices.all { flowOf(it) == it.expectedState }

    private fun flowOf(v: Node): Boolean {
        return when (v) {
            is InputNode -> v.state
            is UnaryGatedNode -> {
                val parent: Node = graph.getEdgeSource(graph.incomingEdgesOf(v).firstOrNull() ?: return false)
                v.gateFunction.calculate(stateMap.getOrDefault(parent, flowOf(parent)))
            }
            is BinaryGatedNode -> {
                val parentList: List<Node> = graph.incomingEdgesOf(v).map { graph.getEdgeSource(it) }
                if (parentList.size != 2)
                    return false
                val input1 = stateMap.getOrDefault(parentList[0], flowOf(parentList[0]))
                val input2 = stateMap.getOrDefault(parentList[1], flowOf(parentList[1]))
                v.gateFunction.calculate(input1, input2)
            }
            else -> flowOf(graph.getEdgeSource(graph.incomingEdgesOf(v).firstOrNull() ?: return false))
        }.also { stateMap[v] = it }
    }
}