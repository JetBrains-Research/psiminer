package psi.graphs.edgeProviders.php

import astminer.featureextraction.className
import com.intellij.psi.PsiElement
import com.intellij.psi.controlFlow.*
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlow
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpConditionInstruction
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpHostInstruction
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpInstruction
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpReturnInstruction
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpThrowInstruction
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl
import org.slf4j.LoggerFactory
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider
import psi.methodRoot

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
        println(instruction.toString() + " -> " + instruction.anchor + " : " + instruction.className())
        return instruction.anchor
    }

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
                val toInstruction = instructions[toIndex]
                if (!toInstruction.isBranchingInstruction()) {
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

    private fun PhpInstruction.isBranchingInstruction() =
        this is PhpHostInstruction || this is PhpConditionInstruction

    private fun computeEdgeType(instruction: PhpInstruction, hasNextInstruction: Boolean) = when (instruction) {
        is PhpReturnInstruction -> EdgeType.ReturnsTo
        is PhpThrowInstruction -> EdgeType.ThrowsTo
        else -> EdgeType.ControlElement
    }

    private fun provideEdgesForInstruction(
        index: Int,
        instructions: List<PhpInstruction>,
        elements: List<PsiElement?>,
        successors: Array<out Set<Int>>,
        nextInstructions: Array<out Set<Int>>,
        newEdges: MutableList<Edge>,
    ) {
        val instruction = instructions[index]
        val element = elements[index]
        if (!instruction.isBranchingInstruction() && instruction !is PhpEntryPointInstruction) {
            nextInstructions[index].forEach { toIndex ->
                val toElement = elements[toIndex]
                if (toElement != element && element != null && toElement != null) {
                    newEdges.add(Edge(element, toElement, EdgeType.ControlFlow))
                }
            }
        }
        for (toIndex in successors[index]) {
            val hasNextInstruction = toIndex < instructions.size
            val edgeType = computeEdgeType(instruction, hasNextInstruction)
            if (hasNextInstruction) {
                val toInstruction = instructions[toIndex]
                val toElement = elements[toIndex]
                if (
                    element != toElement &&
                    (instruction.isBranchingInstruction() || toInstruction.isBranchingInstruction())
                    && element != null && toElement != null
                ) {
                    newEdges.add(Edge(element, toElement, edgeType))
                }
            } else {
                val methodRoot = element?.methodRoot()
                if (element != null && methodRoot != null) {
                    newEdges.add(Edge(element, methodRoot, edgeType))
                }
            }
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
        val nextInstructions = computeNextNonBranchingInstructions(instructions, successors)
        instructions.indices.forEach { index ->
            provideEdgesForInstruction(index, instructions, elements, successors, nextInstructions, newEdges)
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
