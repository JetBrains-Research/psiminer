package psi.graphs

import com.intellij.psi.PsiElement
import psi.graphs.edgeProviders.EdgeProvider
import psi.preOrder

class CodeGraph(val root: PsiElement) {

    private val edges: EdgeCollection = mutableMapOf()

    fun getEdgesOfType(type: EdgeType) = edges.getOrPut(type) { mutableMapOf() }

    fun getAdjacentEdgesOfType(
        from: PsiElement,
        type: EdgeType
    ): MutableList<Edge> = getEdgesOfType(type).getOrPut(from) { mutableListOf() }

    fun getAdjacentEdgesOfType(
        from: PsiElement,
        type: EdgeType,
        useReversed: Boolean,
    ): List<Edge> = if (useReversed) {
        getAdjacentEdgesOfType(from, type)
    } else {
        getAdjacentEdgesOfType(from, type).filter { !it.reversed }
    }

    fun traverseGraph(edgeTypes: Set<EdgeType>, useReversed: Boolean, visitVertex: (PsiElement) -> Unit) {
        val visited = mutableSetOf<PsiElement>()

        fun dfs(v: PsiElement) {
            visited.add(v)
            visitVertex(v)
            edgeTypes.forEach { edgeType ->
                getAdjacentEdgesOfType(v, edgeType, useReversed).forEach { edge ->
                    if (!visited.contains(edge.to)) {
                        dfs(edge.to)
                    }
                }
            }
        }

        root.preOrder().forEach { v ->
            if (!visited.contains(v)) {
                dfs(v)
            }
        }
    }

    fun getAllNodes(): Set<PsiElement> = edges.flatMap { it.value.keys }.toSet()

    fun getAllEdges(): List<Edge> = edges.flatMap { (_, adjList) ->
        adjList.values.flatten()
    }

    fun <T : EdgeProvider> acceptEdgeProvider(edgeProvider: T): CodeGraph {
        val newEdges = edgeProvider.provideEdges(this)
        newEdges.forEach { edge ->
            getAdjacentEdgesOfType(edge.from, edge.type).add(edge)
            getAdjacentEdgesOfType(edge.to, edge.type).add(edge.reversed())
        }
        return this
    }
}
