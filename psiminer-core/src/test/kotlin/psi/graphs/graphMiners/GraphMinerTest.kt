package psi.graphs.graphMiners

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider

internal class GraphMinerTest {

    @Test
    fun `test change provider ordering`() {
        // This test requires graph miner to swap the given providers
        val graphMiner = StubGraphMiner1()
        graphMiner.mine(mockk())
        assertEquals(
            listOf(anotherEdgeProvider, edgeProvider),
            graphMiner.providersOrder
        )
    }

    @Test
    fun `test keep provider ordering`() {
        // This test requires graph miner to keep the order of given providers
        val graphMiner = StubGraphMiner2()
        graphMiner.mine(mockk())
        assertEquals(
            listOf(anotherEdgeProvider, edgeProvider),
            graphMiner.providersOrder
        )
    }

    companion object {
        val mockEdgeType = mockk<EdgeType>()
        val anotherMockEdgeType = mockk<EdgeType>()

        private val edgeProvider = StubEdgeProvider()
        private val anotherEdgeProvider = AnotherStubEdgeProvider()

        private class StubEdgeProvider :
            EdgeProvider(dependsOn = setOf(anotherMockEdgeType), providedType = mockEdgeType) {
            override fun provideEdges(graph: CodeGraph): List<Edge> = emptyList()
        }

        private class AnotherStubEdgeProvider :
            EdgeProvider(dependsOn = emptySet(), providedType = anotherMockEdgeType) {
            override fun provideEdges(graph: CodeGraph): List<Edge> = emptyList()
        }

        private class StubGraphMiner1 : GraphMiner(setOf(mockEdgeType, anotherMockEdgeType)) {
            override val edgeProviders: Map<EdgeType, EdgeProvider> = mapOf(
                mockEdgeType to edgeProvider,
                anotherMockEdgeType to anotherEdgeProvider
            )
        }

        private class StubGraphMiner2 : GraphMiner(setOf(anotherMockEdgeType, mockEdgeType)) {
            override val edgeProviders: Map<EdgeType, EdgeProvider> = mapOf(
                mockEdgeType to edgeProvider,
                anotherMockEdgeType to anotherEdgeProvider
            )
        }
    }
}
