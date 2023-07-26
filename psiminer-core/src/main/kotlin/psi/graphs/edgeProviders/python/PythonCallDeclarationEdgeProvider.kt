package psi.graphs.edgeProviders.python

import com.jetbrains.python.psi.PyCallExpression
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider

class PythonCallDeclarationEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.Ast),
    providedType = EdgeType.CallDeclaration
) {
    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        graph.vertices.filterIsInstance<PyCallExpression>().forEach { call ->
            val callee = call.callee
            val declaration = callee?.reference?.resolve()
            if (callee != null && declaration != null && graph.vertices.contains(declaration)) {
                newEdges.add(Edge(callee, declaration, EdgeType.CallDeclaration))
            }
        }
        return newEdges
    }
}
