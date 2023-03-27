package psi.graphs.edgeProviders.php

import com.jetbrains.php.lang.psi.elements.impl.VariableImpl
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider

class PhpDeclarationUsageEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.Ast),
    providedType = EdgeType.DeclarationUsage,
) {
    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        graph.vertices.filterIsInstance<VariableImpl>().forEach { vertex ->
            val declaration = vertex.reference?.resolve()
            if (declaration != null) {
                newEdges.add(Edge(declaration, vertex, EdgeType.DeclarationUsage))
            }
        }
        return newEdges
    }
}
