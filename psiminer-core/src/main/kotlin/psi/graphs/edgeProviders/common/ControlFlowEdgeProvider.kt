package psi.graphs.edgeProviders.common

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.controlFlow.AllVariablesControlFlowPolicy
import com.intellij.psi.controlFlow.ControlFlow
import com.intellij.psi.controlFlow.ControlFlowFactory
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider

class ControlFlowEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.Ast),
    providedType = EdgeType.ControlFlow
) {

    private val PsiElement.controlFlow: ControlFlow
        get() = ControlFlowFactory.getInstance(project).getControlFlow(
            this,
            AllVariablesControlFlowPolicy.getInstance()
        )

    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        graph.traverseGraph(setOf(EdgeType.Ast), false) { vertex ->
            if (vertex is PsiMethod) {
                val controlFlow = vertex.body?.controlFlow ?: return@traverseGraph
                val instructions = controlFlow.instructions
                instructions.withIndex().forEach { (index, instruction) ->
                    val from = controlFlow.getElement(index)
                    for (no in 0 until instruction.nNext()) {
                        val toInstruction = instruction.getNext(index, no)
                        if (toInstruction < instructions.size) {
                            val to = controlFlow.getElement(toInstruction)
                            newEdges.add(Edge(from, to, EdgeType.ControlFlow))
                        }
                    }
                }
            }
        }
        return newEdges
    }
}
