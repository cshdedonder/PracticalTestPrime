package graph

import org.jgrapht.graph.SimpleDirectedGraph
import java.io.Serializable
import java.util.*
import org.jgrapht.graph.DefaultEdge as Edge

interface Information : Serializable

interface InformationReducible {
    val information: Information
}

abstract class TileGraph : SimpleDirectedGraph<Node, Edge>(Edge::class.java), InformationReducible {

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

data class Connection(val from: Compass16, val to: Compass16) : Serializable

data class ConnectorInformation(val connections: Set<Connection>) : Information

/**
 * Directed [connections].
 */
class ConnectorTileGraph(private val connections: Collection<Pair<Compass16, Compass16>>) : TileGraph(), Rotatable<ConnectorTileGraph> {

    init {
        connections.forEach { (n1, n2) -> addEdge(externalNodeMap.getValue(n1), externalNodeMap.getValue(n2)) }
    }

    override val information: ConnectorInformation by lazy {
        ConnectorInformation(connections.map { (f, t) -> Connection(f, t) }.toSet())
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

data class InputInformation(val state: Boolean, val outputs: Set<Compass16>) : Information

class InputTileGraph(state: Boolean, outputs: Collection<Compass16>) : TileGraph() {

    override val information: InputInformation by lazy { InputInformation(state, outputs.toSet()) }

    init {
        val node = InputNode(state)
        addVertex(node)
        outputs.forEach { addEdge(node, externalNodeMap.getValue(it)) }
    }
}

data class OutputInformation(val expectedState: Boolean, val inputs: Set<Compass16>) : Information

class OutputTileGraph(expectedState: Boolean, inputs: Collection<Compass16>) : TileGraph() {

    override val information: OutputInformation by lazy { OutputInformation(expectedState, inputs.toSet()) }

    init {
        val node = OutputNode(expectedState)
        addVertex(node)
        inputs.forEach { addEdge(externalNodeMap.getValue(it), node) }
    }
}

data class UnaryGateInformation(val functionDescription: String, val input: Compass16, val outputs: Set<Compass16>) : Information

class UnaryGatedTileGraph(f: UnaryGateFunction, input: Compass16, outputs: Collection<Compass16>) : TileGraph() {

    override val information: UnaryGateInformation by lazy { UnaryGateInformation(f.toString(), input, outputs.toSet()) }

    init {
        val node = UnaryGatedNode(f)
        addVertex(node)
        addEdge(externalNodeMap.getValue(input), node)
        outputs.forEach { addEdge(node, externalNodeMap.getValue(it)) }
    }
}

data class BinaryGateInformation(val functionDescription: String, val input1: Compass16, val input2: Compass16, val outputs: Set<Compass16>) : Information

class BinaryGatedTileGraph(f: BinaryGateFunction, input1: Compass16, input2: Compass16, outputs: Collection<Compass16>) : TileGraph() {

    override val information: BinaryGateInformation by lazy { BinaryGateInformation(f.toString(), input1, input2, outputs.toSet()) }

    init {
        val node = BinaryGatedNode(f)
        addVertex(node)
        addEdge(externalNodeMap.getValue(input1), node)
        addEdge(externalNodeMap.getValue(input2), node)
        outputs.forEach { addEdge(node, externalNodeMap.getValue(it)) }
    }
}