package psi.graphs.graphMiners

import com.intellij.psi.PsiElement
import psi.graphs.CodeGraph
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.BaseEdgeProvider

abstract class BaseGraphMiner(private val edgeTypesToMine: Set<EdgeType>) {

    abstract val edgeProviders: Map<EdgeType, BaseEdgeProvider>

    private val providersOrder: List<BaseEdgeProvider> = edgeTypesToMine.map { edgeType ->
        edgeProviders[edgeType] ?: throw UnsupportedEdgeTypeException(edgeType)
    }.let { edgeProviders ->
        val providersList = mutableListOf<BaseEdgeProvider>()
        val extractedTypes = mutableSetOf<EdgeType>()
        repeat(edgeProviders.size) {
            providersList.forEach { edgeProvider ->
                if (
                    !extractedTypes.contains(edgeProvider.providedType) &&
                    edgeProvider.dependsOn.all { extractedTypes.contains(it) }
                ) {
                    providersList.add(edgeProvider)
                    extractedTypes.add(edgeProvider.providedType)
                    return@repeat
                }
            }
        }
        providersList
    }

    fun mine(root: PsiElement): CodeGraph {
        val codeGraph = CodeGraph(root)
        providersOrder.forEach { edgeProvider ->
            codeGraph.acceptEdgeProvider(edgeProvider)
        }
        return codeGraph
    }

    companion object {
        class UnsupportedEdgeTypeException(edgeType: EdgeType) :
            Exception("Unsupported edge type $edgeType")
    }
}
