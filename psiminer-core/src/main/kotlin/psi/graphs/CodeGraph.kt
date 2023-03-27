package psi.graphs

import com.intellij.psi.PsiElement
import psi.graphs.edgeProviders.EdgeProvider
import psi.nodeProperties.isHidden
import psi.preOrder

class CodeGraph(val root: PsiElement) {

    val vertices: List<PsiElement> = root.preOrder()
    private val verticesSet = vertices.filter { !it.isHidden }. toSet()
    val edges: EdgeCollection = mutableMapOf()
    private val variableDeclarationCache: MutableMap<PsiElement, PsiElement?> = mutableMapOf()

    private fun identifyVariableDeclaration(vertex: PsiElement): PsiElement? {
        val declarationEdge = edges.withType(EdgeType.DeclarationUsage).from(vertex).backward().firstOrNull()
        return declarationEdge?.to
    }

    fun toVariableDeclaration(vertex: PsiElement): PsiElement? =
        variableDeclarationCache.getOrPut(vertex) { identifyVariableDeclaration(vertex) }

    fun getAllEdges(): List<Edge> = edges.flatMap { (_, adjList) ->
        adjList.values.flatten()
    }

    fun <T : EdgeProvider> acceptEdgeProvider(edgeProvider: T): CodeGraph {
        val newEdges = edgeProvider.provideEdges(this).distinct()
        newEdges.forEach { edge ->
            if (verticesSet.contains(edge.from) && verticesSet.contains(edge.to)) {
                edges.withType(edge.type).from(edge.from).add(edge)
                edges.withType(edge.type).from(edge.to).add(edge.reversed())
            }
        }
        return this
    }
}
