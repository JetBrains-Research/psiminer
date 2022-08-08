package psi.graphs.edgeProviders.common

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import psi.graphs.*
import psi.graphs.edgeProviders.EdgeProvider

class NextUsageEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.ControlFlow, EdgeType.DeclarationUsage),
    providedType = EdgeType.NextUsage
) {
    companion object {
        data class NextUsageVertex(
            val vertex: PsiElement,
            val variableDeclaration: PsiElement,
            val controlFlowVertex: PsiElement
        )

        data class DfsState(
            val graph: CodeGraph,
            val visited: MutableSet<PsiElement>,
            val controlFlowToVertexMapping: Map<PsiElement, List<NextUsageVertex>>,
            val newEdges: Edges,
        )
    }

    private fun buildVertexMapping(graph: CodeGraph) =
        graph.vertices.filterIsInstance<PsiIdentifier>().mapNotNull { vertex ->
            val variableDeclaration = graph.toVariableDeclaration(vertex)
            variableDeclaration?.let {
                val controlFlowEdges = graph.edges.withType(EdgeType.ControlFlow)
                var controlFlowVertex: PsiElement? = vertex
                while (
                    controlFlowVertex != null &&
                    controlFlowEdges.from(controlFlowVertex).isEmpty()
                ) {
                    controlFlowVertex = controlFlowVertex.parent
                }
                controlFlowVertex?.let {
                    NextUsageVertex(vertex, variableDeclaration, controlFlowVertex)
                }
            }
        }

    private fun findNextUsages(
        vertex: PsiElement,
        initialVertex: PsiElement,
        variableDeclaration: PsiElement,
        dfsState: DfsState,
    ) {
        dfsState.visited.add(vertex)
        dfsState.graph.edges.withType(EdgeType.ControlFlow).from(vertex).forward().forEach { edge ->
            if (!dfsState.visited.contains(edge.to)) {
                val nextDeclarations = dfsState.controlFlowToVertexMapping[edge.to]
                val nextUsage = nextDeclarations?.firstOrNull { (_, nextVariableDeclaration, _) ->
                    variableDeclaration == nextVariableDeclaration
                }
                if (nextUsage != null) {
                    dfsState.newEdges.add(Edge(initialVertex, nextUsage.vertex, EdgeType.NextUsage))
                } else {
                    findNextUsages(
                        edge.to,
                        initialVertex,
                        variableDeclaration,
                        dfsState
                    )
                }
            }
        }
    }

    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        val vertices: List<NextUsageVertex> = buildVertexMapping(graph)
        val controlFlowToVertexMapping = vertices.groupBy { it.controlFlowVertex }.toMap()
        vertices.forEach { (vertex, variableDeclaration, controlFlowVertex) ->
            val visited: MutableSet<PsiElement> = mutableSetOf()
            findNextUsages(
                controlFlowVertex,
                vertex,
                variableDeclaration,
                DfsState(graph, visited, controlFlowToVertexMapping, newEdges)
            )
        }
        return newEdges
    }
}
