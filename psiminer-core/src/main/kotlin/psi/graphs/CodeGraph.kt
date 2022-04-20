package psi.graphs

import com.intellij.psi.PsiElement
import psi.graphs.edgeProviders.EdgeProvider
import psi.preOrder

class CodeGraph(val root: PsiElement) {
    private val edges: MutableMap<PsiElement, MutableList<Edge>> = mutableMapOf()

    fun getAdjacentEdges(from: PsiElement): MutableList<Edge> = edges.getOrPut(from) { mutableListOf() }

    fun getAdjacentEdgesOfTypes(from: PsiElement, types: Set<EdgeType>, returnReversed: Boolean): List<Edge> =
        getAdjacentEdges(from).filter { edge ->
            (!edge.reversed || returnReversed) && types.contains(edge.type)
        }.toList()

    fun traverseGraph(edgeTypes: Set<EdgeType>, useReversed: Boolean, visitVertex: (PsiElement) -> Unit) {
        val visited = mutableSetOf<PsiElement>()

        fun dfs(v: PsiElement) {
            visited.add(v)
            visitVertex(v)
            getAdjacentEdgesOfTypes(v, edgeTypes, useReversed).forEach { (_, to, _) ->
                if (!visited.contains(to)) {
                    dfs(to)
                }
            }
        }

        root.preOrder().forEach { v ->
            if (!visited.contains(v)) {
                dfs(v)
            }
        }
    }

    fun getAllNodes(): Set<PsiElement> = edges.keys

    fun getAllEdges(): List<Edge> = edges.values.flatten()

    fun <T : EdgeProvider> acceptEdgeProvider(edgeProvider: T): CodeGraph {
        val newEdges = edgeProvider.provideEdges(this)
        newEdges.forEach { edge ->
            getAdjacentEdges(edge.from).add(edge)
            getAdjacentEdges(edge.to).add(edge.reversed())
        }
        return this
    }
}
