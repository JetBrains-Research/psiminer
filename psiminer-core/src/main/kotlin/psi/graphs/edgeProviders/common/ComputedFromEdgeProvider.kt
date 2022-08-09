package psi.graphs.edgeProviders.common

import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider
import psi.language.LanguageHandler

class ComputedFromEdgeProvider(
    private val languageHandler: LanguageHandler
) : EdgeProvider(
    dependsOn = setOf(EdgeType.Ast),
    providedType = EdgeType.ComputedFrom
) {
    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        val assignmentProvider = languageHandler.assignmentProvider
        val assignments = assignmentProvider.getAllAssignments(graph.root)
        assignments.forEach { assignment ->
            val leftVariables = assignmentProvider.getLeftVariables(assignment)
            val rightVariables = assignmentProvider.getRightVariables(assignment)
            leftVariables.forEach { lVar ->
                rightVariables.forEach { rVar ->
                    newEdges.add(Edge(lVar, rVar, EdgeType.ComputedFrom))
                }
            }
        }
        return newEdges
    }
}
