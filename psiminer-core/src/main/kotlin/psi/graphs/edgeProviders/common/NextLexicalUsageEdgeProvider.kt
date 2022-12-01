package psi.graphs.edgeProviders.common

import psi.graphs.*
import psi.graphs.edgeProviders.EdgeProvider

class NextLexicalUsageEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.DeclarationUsage),
    providedType = EdgeType.NextLexicalUsage
) {
    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        graph.vertices.groupBy { vertex ->
            graph.toVariableDeclaration(vertex)
        }.forEach { (variableDeclaration, variableUsages) ->
            variableDeclaration?.let {
                variableUsages.zipWithNext { usage1, usage2 ->
                    newEdges.add(Edge(usage1, usage2, EdgeType.NextLexicalUsage))
                }
            }
        }
        return newEdges
    }
}
