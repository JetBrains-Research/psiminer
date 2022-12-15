package psi.graphs.edgeProviders.php

import com.jetbrains.php.lang.psi.elements.impl.VariableImpl
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider

class PhpDeclarationUsageEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.Ast),
    providedType = EdgeType.DeclarationUsage
) {
    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        graph.vertices.filterIsInstance<VariableImpl>().forEach { vertex ->
            val firstAssignmentVertex = graph.vertices.firstOrNull { it is VariableImpl && it.text == vertex.text }
            if (firstAssignmentVertex != null) {
                newEdges.add(Edge(firstAssignmentVertex, vertex, EdgeType.DeclarationUsage))
            }
        }
        return newEdges
    }
}
