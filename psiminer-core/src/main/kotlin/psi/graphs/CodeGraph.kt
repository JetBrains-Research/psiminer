package psi.graphs

import com.intellij.psi.PsiElement
import psi.graphs.edgeProviders.BaseEdgeProvider
import psi.preOrder

abstract class CodeGraph(val root: PsiElement) {
    private val edges: MutableMap<PsiElement, MutableList<Edge>> = mutableMapOf()

    private fun getAdjacentEdges(from: PsiElement): MutableList<Edge> = edges.getOrPut(from) { mutableListOf() }

    private fun getAdjacentEdgesOfTypes(from: PsiElement, types: Set<EdgeType>, returnReversed: Boolean): List<Edge> =
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

    fun <T : BaseEdgeProvider> acceptEdgeProvider(edgeProvider: T) {
        val newEdges = edgeProvider.provideEdges(this)
        newEdges.forEach { edge ->
            getAdjacentEdges(edge.from).add(edge)
            getAdjacentEdges(edge.to).add(edge.reversed())
        }
    }
}
