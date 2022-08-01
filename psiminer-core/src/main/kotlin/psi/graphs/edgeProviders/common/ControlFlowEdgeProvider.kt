package psi.graphs.edgeProviders.common

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.controlFlow.AllVariablesControlFlowPolicy
import com.intellij.psi.controlFlow.ControlFlow
import com.intellij.psi.controlFlow.ControlFlowFactory
import com.intellij.psi.controlFlow.GoToInstruction
import com.intellij.psi.controlFlow.Instruction
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
                    if (instruction !is GoToInstruction) {
                        val from = controlFlow.getElement(index)
                        for (no in 0 until instruction.nNext()) {
                            val toIndex = retrieveNextInstruction(instruction, instructions, index, no)
                            if (toIndex != null) {
                                val to = controlFlow.getElement(toIndex)
                                newEdges.add(Edge(from, to, EdgeType.ControlFlow))
                            }
                        }
                    }
                }
            }
        }
        return newEdges
    }

    companion object {
        private fun retrieveNextInstruction(
            curInstruction: Instruction,
            instructions: List<Instruction>,
            index: Int,
            no: Int,
        ): Int? {
            val toIndex = curInstruction.getNext(index, no)
            return if (toIndex < instructions.size) {
                val instruction = instructions[toIndex]
                if (instruction is GoToInstruction) {
                    retrieveNextInstruction(instruction, instructions, toIndex, 0)
                } else {
                    toIndex
                }
            } else {
                null
            }
        }
    }
}
