package psi.graphs.edgeProviders.php

import com.intellij.psi.PsiElement
import com.intellij.psi.controlFlow.*
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlow
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpExitPointInstruction
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpInstruction
import com.jetbrains.php.lang.psi.elements.ControlStatement
import com.jetbrains.php.lang.psi.elements.PhpReturn
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl
import org.slf4j.LoggerFactory
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider

class PhpControlFlowEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.Ast),
    providedType = EdgeType.ControlFlow
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val PsiElement.controlFlow: ControlFlow
        get() = ControlFlowFactory.getInstance(project).getControlFlow(
            this,
            AllVariablesControlFlowPolicy.getInstance()
        )

    private fun getPsiElementFromControlFlow(controlFlow: PhpControlFlow, index: Int): PsiElement? {
        val instruction = controlFlow.instructions[index]
        println(instruction.toString() + "  " + instruction.anchor + "  " + instruction.predecessors.joinToString(" "))
        return instruction.anchor
    }

    /**
     * For some strange reason in PhpInstruction we can only access its predecessors.
     * This function uses this information to create set of successors for each instruction.
     */
    private fun calculateSuccessors(instructions: List<PhpInstruction>): Array<out Set<Int>> {
        val n = instructions.size
        val successors = Array<MutableSet<Int>>(n) { mutableSetOf() }
        instructions.forEach { instruction ->
            instruction.predecessors.forEach { predecessor ->
                successors[predecessor.num()].add(instruction.num())
            }
        }
        return successors
    }

    private fun computeNextNonBranchingInstructions(
        instructions: List<PhpInstruction>,
        elements: List<PsiElement?>,
        successors: Array<out Set<Int>>
    ): Array<out Set<Int>> {
        val n = instructions.size
        val nextInstructions = Array<MutableSet<Int>>(n) { mutableSetOf() }
        val used = BooleanArray(n) { false }

        fun dfsFromInstruction(index: Int) {
            if (used[index]) {
                return
            }
            used[index] = true
            for (toIndex in successors[index]) {
                val toElement = elements[toIndex]
                if (!toElement.correspondsToBranchingInstruction()) {
                    nextInstructions[index].add(toIndex)
                } else {
                    dfsFromInstruction(toIndex)
                    nextInstructions[index].addAll(nextInstructions[toIndex])
                }
            }
        }

        for (index in 0 until n) {
            dfsFromInstruction(index)
        }
        return nextInstructions
    }

    private fun PsiElement?.correspondsToBranchingInstruction(): Boolean =
        this is ControlStatement || this is PhpReturn

    private fun provideEdgesForInstruction(
        index: Int,
        instructions: List<PhpInstruction>,
        elements: List<PsiElement?>,
        successors: Array<out Set<Int>>,
        nextInstructions: Array<out Set<Int>>,
    ): MutableList<Edge> {
        val newEdges = mutableListOf<Edge>()
        val element = elements[index] ?: return mutableListOf()
        if (!element.correspondsToBranchingInstruction()) {
            nextInstructions[index].forEach { toIndex ->
                val toElement = elements[toIndex]
                if (toElement != element && toElement != null) {
                    newEdges.add(Edge(element, toElement, EdgeType.ControlFlow))
                }
            }
        }
        for (toIndex in successors[index]) {
            val edgeType = EdgeType.ControlElement
            val toElement = elements[toIndex] ?: continue
            if (
                element != toElement &&
                (element.correspondsToBranchingInstruction() || toElement.correspondsToBranchingInstruction())
            ) {
                newEdges.add(Edge(element, toElement, edgeType))
            }
        }
        if (element is PhpReturn || isTerminalInstruction(index, instructions, successors)) {
            addReturnsToEdge(element, elements, newEdges)
        }
        return newEdges
    }

    private fun isTerminalInstruction(
        index: Int,
        instructions: List<PhpInstruction>,
        successors: Array<out Set<Int>>,
    ): Boolean =
        successors[index].isEmpty() ||
                successors[index].map { instructions[it] }.any { it is PhpExitPointInstruction }

    private fun addReturnsToEdge(
        element: PsiElement,
        elements: List<PsiElement?>,
        newEdges: MutableList<Edge>
    ) {
        val methodRoot = elements[0] ?: return
        if (methodRoot is MethodImpl) {
            newEdges.add(Edge(element, methodRoot, EdgeType.ReturnsTo))
        }
    }

    private fun provideEdgesForMethod(vertex: MethodImpl, newEdges: MutableList<Edge>) {
        val controlFlow = try {
            vertex.controlFlow
        } catch (e: AnalysisCanceledException) {
            logger.warn("Ignored method due to exception in control flow construction: ${e.message}")
            return
        }
        val instructions = controlFlow.instructions.toList()
        val elements = instructions.indices.map { index -> getPsiElementFromControlFlow(controlFlow, index) }
        val successors = calculateSuccessors(instructions)
        val nextInstructions = computeNextNonBranchingInstructions(instructions, elements, successors)
        instructions.indices.forEach { index ->
            newEdges.addAll(provideEdgesForInstruction(index, instructions, elements, successors, nextInstructions))
        }
    }

    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        graph.vertices.filterIsInstance<MethodImpl>().forEach { vertex ->
            provideEdgesForMethod(vertex, newEdges)
        }
        return newEdges
    }
}
