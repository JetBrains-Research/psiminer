package psi.graphs.edgeProviders.java

import IncorrectMiningStateException
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

class JavaControlFlowEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.Ast),
    providedType = EdgeType.ControlFlow
) {

    private val PsiElement.controlFlow: ControlFlow
        get() = ControlFlowFactory.getInstance(project).getControlFlow(
            this,
            AllVariablesControlFlowPolicy.getInstance()
        )

    private fun provideEdgesForGoToInstruction(
        index: Int,
        instruction: GoToInstruction,
        controlFlow: ControlFlow,
        newEdges: MutableList<Edge>
    ) {
        if (instruction.isReturn) {
            val nNext = instruction.nNext()
            if (nNext != 1) {
                throw IncorrectMiningStateException("Unexpected number of next for GoTo instruction: $nNext")
            }
            val toIndex = instruction.getNext(index, 0)

            val curElement = controlFlow.getElement(index)
            val nextElement = if (toIndex < controlFlow.instructions.size) {
                controlFlow.getElement(toIndex)
            } else {
                var element = curElement
                while (element !is PsiMethod) {
                    element = element.parent
                }
                element
            }
            newEdges.add(Edge(curElement, nextElement, EdgeType.ReturnsTo))
        }
    }

    private fun provideEdgesForIntermediateInstruction(
        index: Int,
        instruction: Instruction,
        instructions: List<Instruction>,
        controlFlow: ControlFlow,
        newEdges: MutableList<Edge>
    ) {
        val from = controlFlow.getElement(index)
        for (no in 0 until instruction.nNext()) {
            val toIndex = retrieveNextInstruction(instruction, instructions, index, no)
            if (toIndex != null) {
                val to = controlFlow.getElement(toIndex)
                newEdges.add(Edge(from, to, EdgeType.ControlFlow))
            }
        }
    }

    private fun provideEdgesForMethod(vertex: PsiMethod, newEdges: MutableList<Edge>) {
        val controlFlow = vertex.body?.controlFlow ?: return
        val instructions = controlFlow.instructions
        instructions.withIndex().forEach { (index, instruction) ->
            if (instruction is GoToInstruction) {
                provideEdgesForGoToInstruction(index, instruction, controlFlow, newEdges)
            } else {
                provideEdgesForIntermediateInstruction(index, instruction, instructions, controlFlow, newEdges)
            }
        }
    }

    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        graph.vertices.filterIsInstance<PsiMethod>().forEach { vertex ->
            provideEdgesForMethod(vertex, newEdges)
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
