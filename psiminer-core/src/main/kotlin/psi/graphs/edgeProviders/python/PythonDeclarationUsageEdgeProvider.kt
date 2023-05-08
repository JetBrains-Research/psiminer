package psi.graphs.edgeProviders.python

import com.jetbrains.python.psi.PyNamedParameter
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PyTargetExpression
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider

class PythonDeclarationUsageEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.Ast),
    providedType = EdgeType.DeclarationUsage
) {
    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        graph.vertices.filterIsInstance<PyReferenceExpression>().forEach { vertex ->
            val usage = vertex.firstChild
            val declarationRoot = vertex.reference.resolve()
            if (declarationRoot != null &&
                (declarationRoot is PyTargetExpression || declarationRoot is PyNamedParameter)) {
                val declaration = declarationRoot.firstChild
                newEdges.add(Edge(declaration, usage, EdgeType.DeclarationUsage))
            }
        }
        return newEdges
    }
}
