package psi.graphs.edgeProviders.java

import com.intellij.psi.PsiMethodCallExpression
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider
import psi.preOrder

class JavaMethodUsageEdgeProvider: EdgeProvider(
    dependsOn = setOf(EdgeType.Ast),
    providedType = EdgeType.MethodDeclarationUsage,
) {
    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        val fileRoot = graph.root.containingFile
        fileRoot.preOrder().filterIsInstance<PsiMethodCallExpression>().forEach { methodCall ->
            val methodDeclaration = methodCall.methodExpression.resolve() ?: return@forEach
            newEdges.add(Edge(methodDeclaration, methodCall, EdgeType.MethodDeclarationUsage))
        }
        return newEdges
    }
}