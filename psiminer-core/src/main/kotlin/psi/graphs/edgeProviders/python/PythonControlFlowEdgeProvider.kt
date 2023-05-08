package psi.graphs.edgeProviders.python

import com.intellij.codeInsight.controlflow.Instruction
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.jetbrains.python.codeInsight.controlflow.PyControlFlowBuilder
import com.jetbrains.python.psi.PyFunction
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider

class PythonControlFlowEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.Ast),
    providedType = EdgeType.ControlFlow
) {

    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        graph.vertices.filterIsInstance<PyFunction>().forEach { vertex ->
            provideEdgesForMethod(vertex, newEdges)
        }
        return newEdges
    }

    private fun provideEdgesForMethod(vertex: PyFunction, newEdges: MutableList<Edge>) {
        val controlFlow = PyControlFlowBuilder().buildControlFlow(vertex)
        val instructions = controlFlow.instructions
        val elements = instructions.mapIndexed { index, instruction ->
            if (index == 0) {
                vertex
            } else {
                instruction.element
            }
        }
        (instructions.indices).forEach { index ->
            provideEdgesForInstruction(index, instructions.toList(), elements, newEdges)
        }
    }

    private fun provideEdgesForInstruction(
        index: Int,
        instructions: List<Instruction>,
        elements: List<PsiElement?>,
        newEdges: MutableList<Edge>,
    ) {
        val instruction = instructions[index]
        val element = elements[index] ?: return
        instruction.allSucc().forEach { toInstruction ->
            val toIndex = toInstruction.num()
            val toElement = elements[toIndex]
            if (toElement != element && toElement != null) {
                newEdges.add(Edge(element, toElement, EdgeType.ControlFlow))
            }
        }
        if (instruction.isTerminal()) {
            val methodRoot = element.parentOfType<PyFunction>()
            if (methodRoot != null) {
                newEdges.add(Edge(element, methodRoot, EdgeType.ReturnsTo))
            }
        }
    }

    private fun Instruction.isTerminal(): Boolean =
        this.allSucc().all { it.element == null }
}
