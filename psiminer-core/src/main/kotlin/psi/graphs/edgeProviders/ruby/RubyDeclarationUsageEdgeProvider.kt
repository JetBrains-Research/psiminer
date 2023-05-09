package psi.graphs.edgeProviders.ruby

import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RIdentifier
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RVariable
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider

class RubyDeclarationUsageEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.Ast),
    providedType = EdgeType.DeclarationUsage
) {
    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        val usages = graph.vertices.filter {
            it is RIdentifier || it is RVariable
        }
        for (usage in usages) {
            val decl = usage.reference?.resolve() ?: continue
            if (decl != usage && (decl is RIdentifier || decl is RVariable)) {
                newEdges.add(Edge(decl, usage, EdgeType.DeclarationUsage))
            }
        }
        return newEdges
    }
}
