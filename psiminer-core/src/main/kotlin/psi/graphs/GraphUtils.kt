package psi.graphs

import com.intellij.psi.PsiElement

enum class EdgeType {
    // AST edges
    Ast,

    // Auxiliary edges
    NextToken,

    // CFG edges
    ControlFlow,
    ControlElement,
    ReturnsTo,
    ThrowsTo,
    CallDeclaration,

    // DFG edges
    DeclarationUsage,
    NextUsage,
    ComputedFrom,
    NextLexicalUsage,
}

data class Edge(val from: PsiElement, val to: PsiElement, val type: EdgeType, val reversed: Boolean = false) {
    fun reversed(): Edge = Edge(to, from, type, !reversed)
}

typealias Edges = MutableList<Edge>
typealias AdjacencyList = MutableMap<PsiElement, Edges>
typealias EdgeCollection = MutableMap<EdgeType, AdjacencyList>

fun Edges.forward() = this.filter { !it.reversed }

fun Edges.backward() = this.filter { it.reversed }

fun AdjacencyList.from(vertex: PsiElement): Edges = this.getOrPut(vertex) { mutableListOf() }

fun EdgeCollection.withType(edgeType: EdgeType): AdjacencyList = this.getOrPut(edgeType) { mutableMapOf() }
