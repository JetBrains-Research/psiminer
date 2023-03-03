package psi.graphs.edgeProviders.php

import com.intellij.psi.PsiElement
import com.intellij.psi.controlFlow.*
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlow
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpCatchConditionInstruction
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpFinallyEndInstruction
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpInstruction
import com.jetbrains.php.codeInsight.controlFlow.instructions.impl.PhpFinallyHostInstruction
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

    private fun getPsiElementFromControlFlow(controlFlow: PhpControlFlow, index: Int): PsiElement? {
        val instruction = controlFlow.instructions[index]
        return instruction.anchor ?: tryFindFinallyAnchor(instruction, controlFlow, index)
    }

    /**
     * Special case for try-catch-finally statement.
     *
     * Tries to map HOST instruction without anchor to Finally clause.
     * In all other cases simply returns null.
     */
    private fun tryFindFinallyAnchor(
        instruction: PhpInstruction,
        controlFlow: PhpControlFlow,
        index: Int
    ): PsiElement? {
        val prevInstruction = if (index > 0) controlFlow.instructions[index - 1] else return null
        if (instruction is PhpFinallyHostInstruction && prevInstruction is PhpCatchConditionInstruction) {
            val finallyEndInstruction = prevInstruction.predecessors.find { it is PhpFinallyEndInstruction }
            return finallyEndInstruction?.anchor
        }
        return null
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
        val nextNonBranchingInstructions = Array<MutableSet<Int>>(n) { mutableSetOf() }
        val used = BooleanArray(n) { false }

        fun dfsFromInstruction(index: Int) {
            if (used[index]) {
                return
            }
            used[index] = true
            for (toIndex in successors[index]) {
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
        this is ControlStatement || this is PhpReturn

    private fun provideEdgesForInstruction(
        index: Int,
        elements: List<PsiElement?>,
        successors: Array<out Set<Int>>,
        nextNonBranchingInstructions: Array<out Set<Int>>,
    ): MutableList<Edge> {
        val newEdges = mutableListOf<Edge>()
        val element = elements[index] ?: return mutableListOf()
        if (!element.correspondsToBranchingInstruction()) {
            nextNonBranchingInstructions[index].forEach { toIndex ->
                val toElement = elements[toIndex]
                if (toElement != element && toElement != null) {
                    newEdges.add(Edge(element, toElement, EdgeType.ControlFlow))
                }
            }
        }
        for (toIndex in successors[index]) {
            val toElement = elements[toIndex] ?: continue
            if (
                element != toElement &&
                (element.correspondsToBranchingInstruction() || toElement.correspondsToBranchingInstruction())
            ) {
                newEdges.add(Edge(element, toElement, EdgeType.ControlElement))
            }
        }
        if (isTerminalInstruction(index, elements, successors)) {
            addReturnsToEdge(element, elements, newEdges)
        }
        return newEdges
    }

    private fun isTerminalInstruction(
        index: Int,
        elements: List<PsiElement?>,
        successors: Array<out Set<Int>>,
    ): Boolean =
        successors[index].isEmpty() ||
                successors[index].map { elements[it] }.all { it == null }

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
        val nextNonBranchingInstructions = computeNextNonBranchingInstructions(instructions, elements, successors)
        instructions.indices.forEach { index ->
            newEdges.addAll(
                provideEdgesForInstruction(index, elements, successors, nextNonBranchingInstructions)
            )
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
