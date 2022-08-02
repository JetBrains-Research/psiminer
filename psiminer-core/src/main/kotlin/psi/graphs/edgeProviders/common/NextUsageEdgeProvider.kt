package psi.graphs.edgeProviders.common

import com.intellij.psi.PsiElement
import psi.graphs.*
import psi.graphs.edgeProviders.EdgeProvider

class NextUsageEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.ControlFlow, EdgeType.DeclarationUsage),
    providedType = EdgeType.NextUsage
) {
    private fun findNextUsages(
        vertex: PsiElement,
        variableDeclaration: PsiElement,
        graph: CodeGraph,
        visited: MutableSet<PsiElement>,
        newEdges: Edges,
    ) {
        visited.add(vertex)
        graph.edges.withType(EdgeType.ControlFlow).from(vertex).forward().forEach { edge ->
            if (!visited.contains(edge.to)) {
                val nextDeclaration = graph.toVariableDeclaration(edge.to)
                if (nextDeclaration == variableDeclaration) {
                    newEdges.add(Edge(vertex, edge.to, EdgeType.NextUsage))
                } else {
                    findNextUsages(edge.to, variableDeclaration, graph, visited, newEdges)
                }
            }
        }
    }

    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        graph.vertices.forEach { vertex ->
            val variableDeclaration = graph.toVariableDeclaration(vertex) ?: return@forEach
            val visited: MutableSet<PsiElement> = mutableSetOf()
            findNextUsages(vertex, variableDeclaration, graph, visited, newEdges)
        }
        return newEdges
    }
}
