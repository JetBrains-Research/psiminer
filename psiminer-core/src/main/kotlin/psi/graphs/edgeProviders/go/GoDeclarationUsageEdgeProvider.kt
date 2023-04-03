package psi.graphs.edgeProviders.go

import com.goide.psi.GoParamDefinition
import com.goide.psi.GoReferenceExpression
import com.goide.psi.GoVarOrConstDefinition
import com.intellij.psi.impl.source.tree.LeafPsiElement
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider

class GoDeclarationUsageEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.Ast),
    providedType = EdgeType.DeclarationUsage
) {
    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        graph.vertices.filterIsInstance<LeafPsiElement>().forEach { vertex ->
            val parent = vertex.parent
            val declaration = if (parent is GoReferenceExpression) {
                parent.reference.resolve()
            } else {
                parent
            }
            if (declaration is GoVarOrConstDefinition || declaration is GoParamDefinition) {
                newEdges.add(Edge(declaration, vertex, EdgeType.DeclarationUsage))
            }
        }
        return newEdges
    }
}
