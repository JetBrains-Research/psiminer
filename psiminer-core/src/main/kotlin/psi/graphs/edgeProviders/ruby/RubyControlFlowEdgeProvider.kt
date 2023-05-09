package psi.graphs.edgeProviders.ruby

import com.intellij.codeInsight.controlflow.ConditionalInstruction
import com.intellij.codeInsight.controlflow.Instruction
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.RBlockStatement
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.blocks.RCompoundStatement
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider

class RubyControlFlowEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.Ast),
    providedType = EdgeType.ControlFlow
) {

    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        graph.vertices.filterIsInstance<RMethod>().forEach { vertex ->
            provideEdgesForMethod(vertex, newEdges)
        }
        return newEdges
    }

    private fun provideEdgesForMethod(vertex: RMethod, newEdges: MutableList<Edge>) {
        val controlFlow = vertex.controlFlow
        val instructions = controlFlow.instructions.toList()

        val elements = instructions.mapIndexed { index, instruction ->
            if (index == 0) {
                vertex
            } else if (instruction is ConditionalInstruction) {
                instruction.condition.parent
            } else {
                instruction.element
            }
        }
        val nextNonBranchingInstructions = computeNextNonBranchingInstructions(instructions, elements)
        instructions.indices.forEach { index ->
            provideEdgesForInstruction(index, instructions, elements, nextNonBranchingInstructions, newEdges)
        }
    }

    private fun computeNextNonBranchingInstructions(
        instructions: List<Instruction>,
        elements: List<PsiElement?>
    ): Array<out Set<Int>> {
        val n = instructions.size
        val nextNonBranchingInstructions = Array<MutableSet<Int>>(n) { mutableSetOf() }
        val used = BooleanArray(n) { false }

        fun dfsFromInstruction(index: Int) {
            if (used[index]) {
                return
            }
            used[index] = true
            for (toInstruction in instructions[index].allSucc()) {
                val toIndex = toInstruction.num()
                val toElement = elements[toIndex]
                if (!toElement.correspondsToBranchingInstruction()) {
                    nextNonBranchingInstructions[index].add(toIndex)
                } else {
                    dfsFromInstruction(toIndex)
                    nextNonBranchingInstructions[index].addAll(nextNonBranchingInstructions[toIndex])
                }
            }
        }

        for (index in 0 until n) {
            dfsFromInstruction(index)
        }
        return nextNonBranchingInstructions
    }

    private fun PsiElement?.correspondsToBranchingInstruction(): Boolean =
        this is RBlockStatement || this is RCompoundStatement

    private fun provideEdgesForInstruction(
        index: Int,
        instructions: List<Instruction>,
        elements: List<PsiElement?>,
        nextNonBranchingInstructions: Array<out Set<Int>>,
        newEdges: MutableList<Edge>
    ) {
        val element = elements[index] ?: return
        if (!element.correspondsToBranchingInstruction()) {
            provideControlFlowEdges(index, element, elements, nextNonBranchingInstructions, newEdges)
        }
        for (toInstruction in instructions[index].allSucc()) {
            val toIndex = toInstruction.num()
            val toElement = elements[toIndex] ?: continue
            if (
                element != toElement &&
                (element.correspondsToBranchingInstruction() || toElement.correspondsToBranchingInstruction())
            ) {
                newEdges.add(Edge(element, toElement, EdgeType.ControlElement))
            }
        }
    }

    private fun provideControlFlowEdges(
        index: Int,
        element: PsiElement,
        elements: List<PsiElement?>,
        nextNonBranchingInstructions: Array<out Set<Int>>,
        newEdges: MutableList<Edge>
    ) {
        var isFinal = true
        for (toIndex in nextNonBranchingInstructions[index]) {
            val toElement = elements[toIndex] ?: continue
            isFinal = false
            if (toElement != element) {
                newEdges.add(Edge(element, toElement, EdgeType.ControlFlow))
            }
        }
        val methodRoot = elements[0]
        if (isFinal && methodRoot != null) {
            newEdges.add(Edge(element, methodRoot, EdgeType.ReturnsTo))
        }
    }
}
