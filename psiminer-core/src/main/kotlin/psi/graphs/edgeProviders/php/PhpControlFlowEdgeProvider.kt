package psi.graphs.edgeProviders.php

import com.intellij.psi.PsiElement
import com.intellij.psi.controlFlow.*
import com.jetbrains.php.codeInsight.controlFlow.PhpControlFlow
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpEntryPointInstruction
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpInstruction
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
        println(instruction.toString() + " -> " + instruction.anchor + " : " + instruction.predecessors)
        return instruction.anchor
    }

    private fun computeNextNonBranchingInstructions(instructions: List<PhpInstruction>): Array<out Set<Int>> {
        val n = instructions.size
        val nextInstructions = Array<MutableSet<Int>>(n) { mutableSetOf() }

        for (index in 0 until n) {
            val instruction = instructions[index]
            instruction.predecessors.forEach { predecessor ->
                if (predecessor !is PhpEntryPointInstruction) {
                    nextInstructions[predecessor.num()].add(index)
                }
            }
        }
        return nextInstructions
    }

    private fun provideEdgesForInstruction(
        index: Int,
        instructions: List<PhpInstruction>,
        elements: List<PsiElement?>,
        nextInstructions: Array<out Set<Int>>,
        newEdges: MutableList<Edge>,
    ) {
        val instruction = instructions[index]
        val element = elements[index]
        if (instruction !is BranchingInstruction) {
            nextInstructions[index].forEach { toIndex ->
                val toElement = elements[toIndex]
                if (toElement != element && element != null && toElement != null) {
                    newEdges.add(Edge(element, toElement, EdgeType.ControlFlow))
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
        val nextInstructions = computeNextNonBranchingInstructions(instructions)
        instructions.indices.forEach { index ->
            provideEdgesForInstruction(index, instructions, elements, nextInstructions, newEdges)
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
