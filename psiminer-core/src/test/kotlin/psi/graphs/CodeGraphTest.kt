package psi.graphs

import mockedPsi.TinyTree
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import psi.graphs.edgeProviders.EdgeProvider

internal class CodeGraphTest {

    @Test
    fun `test adjacent edges in SmallTree`() {
        val codeGraph = CodeGraph(TinyTree.root)
        codeGraph.acceptEdgeProvider(stubEdgeProvider)

        assertEquals(correctRootEdgesType1, codeGraph.getAdjacentEdges(TinyTree.root))
        assertEquals(correctLeaf1EdgesType1, codeGraph.getAdjacentEdges(TinyTree.leaf1))
        assertEquals(correctLeaf2EdgesType1, codeGraph.getAdjacentEdges(TinyTree.leaf2))
    }

    @Test
    fun `test consecutive edge type providers in SmallTree`() {
        val codeGraph = CodeGraph(TinyTree.root)
        codeGraph.acceptEdgeProvider(stubEdgeProvider)
        codeGraph.acceptEdgeProvider(anotherStubAstEdgeProvider)

        assertEquals(correctRootEdgesType1 + correctRootEdgesType2, codeGraph.getAdjacentEdges(TinyTree.root))
        assertEquals(correctLeaf1EdgesType1 + correctLeaf1EdgesType2, codeGraph.getAdjacentEdges(TinyTree.leaf1))
        assertEquals(correctLeaf2EdgesType1 + correctLeaf2EdgesType2, codeGraph.getAdjacentEdges(TinyTree.leaf2))
    }

    @Test
    fun `test adjacent edges of type in SmallTree`() {
        val codeGraph = CodeGraph(TinyTree.root)
        codeGraph.acceptEdgeProvider(stubEdgeProvider)
        codeGraph.acceptEdgeProvider(anotherStubAstEdgeProvider)

        assertEquals(
            correctRootEdgesType1,
            codeGraph.getAdjacentEdgesOfTypes(TinyTree.root, setOf(mockEdgeType), true)
        )
        assertEquals(
            correctLeaf1EdgesType1,
            codeGraph.getAdjacentEdgesOfTypes(TinyTree.leaf1, setOf(mockEdgeType), true)
        )
        assertEquals(
            correctLeaf2EdgesType1,
            codeGraph.getAdjacentEdgesOfTypes(TinyTree.leaf2, setOf(mockEdgeType), true)
        )
        assertEquals(
            correctRootEdgesType2,
            codeGraph.getAdjacentEdgesOfTypes(TinyTree.root, setOf(anotherMockEdgeType), true)
        )
        assertEquals(
            correctLeaf1EdgesType2,
            codeGraph.getAdjacentEdgesOfTypes(TinyTree.leaf1, setOf(anotherMockEdgeType), true)
        )
        assertEquals(
            correctLeaf2EdgesType2,
            codeGraph.getAdjacentEdgesOfTypes(TinyTree.leaf2, setOf(anotherMockEdgeType), true)
        )
    }

    @Test
    fun `test no reverse edges of type in SmallTree`() {
        val codeGraph = CodeGraph(TinyTree.root)
        codeGraph.acceptEdgeProvider(stubEdgeProvider)
        codeGraph.acceptEdgeProvider(anotherStubAstEdgeProvider)

        assertEquals(
            correctRootEdgesType1,
            codeGraph.getAdjacentEdgesOfTypes(TinyTree.root, setOf(mockEdgeType), false)
        )
        assertEquals(
            emptyList<Edge>(),
            codeGraph.getAdjacentEdgesOfTypes(TinyTree.leaf1, setOf(mockEdgeType), false)
        )
        assertEquals(
            emptyList<Edge>(),
            codeGraph.getAdjacentEdgesOfTypes(TinyTree.leaf2, setOf(mockEdgeType), false)
        )
        assertEquals(
            correctRootEdgesType2,
            codeGraph.getAdjacentEdgesOfTypes(TinyTree.root, setOf(anotherMockEdgeType), false)
        )
        assertEquals(
            emptyList<Edge>(),
            codeGraph.getAdjacentEdgesOfTypes(TinyTree.leaf1, setOf(anotherMockEdgeType), false)
        )
        assertEquals(
            emptyList<Edge>(),
            codeGraph.getAdjacentEdgesOfTypes(TinyTree.leaf2, setOf(anotherMockEdgeType), false)
        )
    }

    companion object {
        val mockEdgeType = mockk<EdgeType>()
        val anotherMockEdgeType = mockk<EdgeType>()

        class StubAstEdgeProvider : EdgeProvider(dependsOn = emptySet(), providedType = mockEdgeType) {
            override fun provideEdges(graph: CodeGraph) = listOf(
                Edge(TinyTree.root, TinyTree.leaf1, mockEdgeType),
                Edge(TinyTree.root, TinyTree.leaf2, mockEdgeType)
            )
        }

        class AnotherStubAstEdgeProvider : EdgeProvider(dependsOn = emptySet(), providedType = anotherMockEdgeType) {
            override fun provideEdges(graph: CodeGraph) = listOf(
                Edge(TinyTree.root, TinyTree.leaf1, anotherMockEdgeType),
                Edge(TinyTree.root, TinyTree.leaf2, anotherMockEdgeType)
            )
        }

        private val stubEdgeProvider = StubAstEdgeProvider()
        private val anotherStubAstEdgeProvider = AnotherStubAstEdgeProvider()

        private val correctRootEdgesType1 = listOf(
            Edge(TinyTree.root, TinyTree.leaf1, mockEdgeType, false),
            Edge(TinyTree.root, TinyTree.leaf2, mockEdgeType, false),
        )
        private val correctLeaf1EdgesType1 = listOf(
            Edge(TinyTree.leaf1, TinyTree.root, mockEdgeType, true),
        )
        private val correctLeaf2EdgesType1 = listOf(
            Edge(TinyTree.leaf2, TinyTree.root, mockEdgeType, true),
        )
        private val correctRootEdgesType2 = listOf(
            Edge(TinyTree.root, TinyTree.leaf1, anotherMockEdgeType, false),
            Edge(TinyTree.root, TinyTree.leaf2, anotherMockEdgeType, false),
        )
        private val correctLeaf1EdgesType2 = listOf(
            Edge(TinyTree.leaf1, TinyTree.root, anotherMockEdgeType, true),
        )
        private val correctLeaf2EdgesType2 = listOf(
            Edge(TinyTree.leaf2, TinyTree.root, anotherMockEdgeType, true),
        )
    }
}
