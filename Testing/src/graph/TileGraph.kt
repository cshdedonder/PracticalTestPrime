package graph

import org.jgrapht.graph.SimpleDirectedGraph
import java.util.*
import org.jgrapht.graph.DefaultEdge as Edge

abstract class TileGraph : SimpleDirectedGraph<Node, Edge>(Edge::class.java) {

    val externalNodeMap: Map<Compass16, Node> = EnumMap<Compass16, Node>(Compass16::class.java).apply {
        Compass16.values().forEach { put(it, Node("Node@$it")) }
    }

    init {
        externalNodeMap.values.forEach { addVertex(it) }
    }
}

interface Rotatable<T> {
    fun rotate(angle: RotationAngle): T
    val rotations: Map<RotationAngle, T>
}

/**
 * Directed [connections].
 */
class ConnectorTileGraph(private val connections: Collection<Pair<Compass16, Compass16>>) : TileGraph(), Rotatable<ConnectorTileGraph> {

    init {
        connections.forEach { (n1, n2) -> addEdge(externalNodeMap.getValue(n1), externalNodeMap.getValue(n2)) }
    }

    override fun rotate(angle: RotationAngle): ConnectorTileGraph {
        return if (angle == RotationAngle.ZERO) {
            this
        } else {
            ConnectorTileGraph(connections.deepMap { it.rotate(angle) })
        }
    }

    private fun <A, B> Collection<Pair<A, A>>.deepMap(f: (A) -> B): Collection<Pair<B, B>> = map { (a1, a2) -> f(a1) to f(a2) }

    override val rotations: Map<RotationAngle, ConnectorTileGraph> by lazy {
        EnumMap<RotationAngle, ConnectorTileGraph>(RotationAngle::class.java).apply {
            RotationAngle.values().forEach { put(it, rotate(it)) }
        }
    }

    override fun toString(): String {
        return connections.joinToString(prefix = "ConnectorTileGraph {", postfix = "}") { (a, b) -> "$a -> $b" }
    }
}

class InputTileGraph(state: Boolean, outputs: Collection<Compass16>) : TileGraph() {

    init {
        val node = InputNode(state)
        addVertex(node)
        outputs.forEach { addEdge(node, externalNodeMap.getValue(it)) }
    }
}

class OutputTileGraph(expectedState: Boolean, inputs: Collection<Compass16>) : TileGraph() {
    init {
        val node = OutputNode(expectedState)
        addVertex(node)
        inputs.forEach { addEdge(externalNodeMap.getValue(it), node) }
    }
}

class UnaryGatedTileGraph(f: UnaryGateFunction, input: Compass16, outputs: Collection<Compass16>) : TileGraph() {
    init {
        val node = UnaryGatedNode(f)
        addVertex(node)
        addEdge(externalNodeMap.getValue(input), node)
        outputs.forEach { addEdge(node, externalNodeMap.getValue(it)) }
    }
}

class BinaryGatedTileGraph(f: BinaryGateFunction, input1: Compass16, input2: Compass16, outputs: Collection<Compass16>) : TileGraph() {
    init {
        val node = BinaryGatedNode(f)
        addVertex(node)
        addEdge(externalNodeMap.getValue(input1), node)
        addEdge(externalNodeMap.getValue(input2), node)
        outputs.forEach { addEdge(node, externalNodeMap.getValue(it)) }
    }
}