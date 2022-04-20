package storage.graphs

import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.utils.mapToIndex
import psi.graphs.CodeGraph
import psi.nodeProperties.nodeType
import psi.nodeProperties.token

object JsonGraphRepresentation {

    @Serializable
    data class GraphRepresentation(
        val edges: EdgesRepresentation,
        val nodes: NodesRepresentation
    )

    @Serializable
    data class EdgesRepresentation(
        val from: List<Int>,
        val to: List<Int>,
        val edgeType: List<String>
    )

    @Serializable
    data class NodesRepresentation(
        val tokens: List<String>,
        val nodeTypes: List<String>
    )

    fun convertCodeGraph(codeGraph: CodeGraph): GraphRepresentation {
        val graphNodes = codeGraph.getAllNodes()
        val nodeToId = graphNodes.mapToIndex()
        val edges = codeGraph.getAllEdges()
        val edgesRepresentation = EdgesRepresentation(
            from = edges.map { nodeToId[it.from] ?: throw IncorrectlyParsedGraph() },
            to = edges.map { nodeToId[it.to] ?: throw IncorrectlyParsedGraph() },
            edgeType = edges.map { if (!it.reversed) "f${it.type}" else "r${it.type}" }
        )
        val nodesRepresentation = NodesRepresentation(
            tokens = graphNodes.map { it.token ?: "" },
            nodeTypes = graphNodes.map { it.nodeType }
        )
        return GraphRepresentation(edgesRepresentation, nodesRepresentation)
    }

    class IncorrectlyParsedGraph : Exception("CodeGraph parsed incorrectly")
}
