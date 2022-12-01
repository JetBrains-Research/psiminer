package psi.graphs.edgeProviders.common

import mockedPsi.SmallTree
import mockedPsi.TinyTree
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType

internal class NextTokenEdgeProviderTest {

    @Test
    fun `test extraction from tiny tree`() {
        val codeGraph = CodeGraph(TinyTree.root)
        codeGraph.acceptEdgeProvider(AstEdgeProvider())

        val edgeProvider = NextTokenEdgeProvider()
        val extractedEdges = edgeProvider.provideEdges(codeGraph)
        val correctEdges = listOf(
            Edge(TinyTree.leaf1, TinyTree.leaf2, EdgeType.NextToken),
        )

        assertEquals(correctEdges, extractedEdges)
    }

    @Test
    fun `test extraction from small tree`() {
        val codeGraph = CodeGraph(SmallTree.root)
        codeGraph.acceptEdgeProvider(AstEdgeProvider())

        val edgeProvider = NextTokenEdgeProvider()
        val extractedEdges = edgeProvider.provideEdges(codeGraph)
        val correctEdges = listOf(
            Edge(SmallTree.leaf1, SmallTree.leaf2, EdgeType.NextToken),
            Edge(SmallTree.leaf2, SmallTree.leaf3, EdgeType.NextToken),
            Edge(SmallTree.leaf3, SmallTree.interNodeRight, EdgeType.NextToken),
        )

        assertEquals(correctEdges, extractedEdges)
    }
}
