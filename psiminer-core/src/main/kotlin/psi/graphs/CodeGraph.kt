package psi.graphs

import com.intellij.psi.PsiElement
import psi.graphs.edgeProviders.EdgeProvider
import psi.preOrder

class CodeGraph(val root: PsiElement) {

    private val vertices: List<PsiElement> = root.preOrder()
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

    fun getAllNodes(): List<PsiElement> = vertices

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
