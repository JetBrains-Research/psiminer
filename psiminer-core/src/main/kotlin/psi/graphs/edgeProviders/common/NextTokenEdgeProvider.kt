package psi.graphs.edgeProviders.common

import com.intellij.psi.PsiElement
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider
import psi.isLeaf

class NextTokenEdgeProvider : EdgeProvider(dependsOn = setOf(EdgeType.Ast), providedType = EdgeType.NextToken) {
    override fun provideEdges(graph: CodeGraph): List<Edge> {
        var previousLeaf: PsiElement? = null
        val newEdges = mutableListOf<Edge>()
        graph.traverseGraph(setOf(EdgeType.Ast), false) { vertex ->
            if (vertex.isLeaf()) {
                previousLeaf?.let { prevLeaf ->
                    newEdges.add(Edge(vertex, prevLeaf, EdgeType.NextToken))
                }
                previousLeaf = vertex
            }
        }
        return newEdges
    }
}
