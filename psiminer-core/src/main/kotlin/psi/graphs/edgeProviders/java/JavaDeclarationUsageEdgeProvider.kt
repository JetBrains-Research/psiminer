package psi.graphs.edgeProviders.java

import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.PsiVariable
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider

class JavaDeclarationUsageEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.Ast),
    providedType = EdgeType.DeclarationUsage
) {
    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        graph.vertices.filterIsInstance<PsiIdentifier>().forEach { vertex ->
            val parent = vertex.parent
            val declaration = if (parent is PsiReferenceExpression) {
                parent.resolve()
            } else {
                parent
            }
            if (declaration is PsiVariable) {
                newEdges.add(Edge(declaration, vertex, EdgeType.DeclarationUsage))
            }
        }
        return newEdges
    }
}
