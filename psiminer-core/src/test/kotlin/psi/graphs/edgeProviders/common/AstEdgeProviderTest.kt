package psi.graphs.edgeProviders.common

import io.mockk.every
import io.mockk.mockk
import mockedPsi.SmallTree
import mockedPsi.TinyTree
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType

internal class AstEdgeProviderTest {

    @Test
    fun `test extraction from tiny tree`() {
        val mockCodeGraph = mockk<CodeGraph>()
        every { mockCodeGraph.root } returns TinyTree.root

        val edgeProvider = AstEdgeProvider()
        val extractedEdges = edgeProvider.provideEdges(mockCodeGraph)
        val correctEdges = listOf(
            Edge(TinyTree.root, TinyTree.leaf1, EdgeType.Ast),
            Edge(TinyTree.root, TinyTree.leaf2, EdgeType.Ast),
        )

        assertEquals(correctEdges, extractedEdges)
    }

    @Test
    fun `test extraction from small tree`() {
        val mockCodeGraph = mockk<CodeGraph>()
        every { mockCodeGraph.root } returns SmallTree.root

        val edgeProvider = AstEdgeProvider()
        val extractedEdges = edgeProvider.provideEdges(mockCodeGraph)
        val correctEdges = listOf(
            Edge(SmallTree.root, SmallTree.interNodeLeft, EdgeType.Ast),
            Edge(SmallTree.root, SmallTree.interNodeMid, EdgeType.Ast),
            Edge(SmallTree.root, SmallTree.interNodeRight, EdgeType.Ast),
            Edge(SmallTree.interNodeLeft, SmallTree.leaf1, EdgeType.Ast),
            Edge(SmallTree.interNodeMid, SmallTree.leaf2, EdgeType.Ast),
            Edge(SmallTree.interNodeMid, SmallTree.leaf3, EdgeType.Ast),
        )

        assertEquals(correctEdges, extractedEdges)
    }
}
