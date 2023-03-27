package psi.graphs.edgeProviders.common

import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider
import psi.isLeaf

class NextTokenEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.Ast),
    providedType = EdgeType.NextToken,
) {
    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        graph.vertices.filter { it.isLeaf() }.zipWithNext().forEach { (from, to) ->
            newEdges.add(Edge(from, to, EdgeType.NextToken))
        }
        return newEdges
    }
}
