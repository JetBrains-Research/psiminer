package psi.graphs.edgeProviders.go

import com.goide.controlflow.GoControlFlowBuilder
import com.goide.controlflow.instructions.GoBranchInstructionBase
import com.goide.controlflow.instructions.GoForLoopEndInstruction
import com.goide.controlflow.instructions.GoForLoopStartInstruction
import com.goide.controlflow.instructions.GoReturnStatementInstruction
import com.goide.psi.GoFunctionOrMethodDeclaration
import com.intellij.codeInsight.controlflow.Instruction
import com.intellij.psi.PsiElement
import org.slf4j.LoggerFactory
import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.edgeProviders.EdgeProvider
import com.intellij.psi.util.parentOfType

class GoControlFlowEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.Ast),
    providedType = EdgeType.ControlFlow
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableSetOf<Edge>()
        graph.vertices.filterIsInstance<GoFunctionOrMethodDeclaration>().forEach { vertex ->
            provideEdgesForFunction(vertex, newEdges)
        }
        return newEdges.toList()
    }

    private fun provideEdgesForFunction(vertex: GoFunctionOrMethodDeclaration, newEdges: MutableSet<Edge>) {
        val controlFlow = try {
            GoControlFlowBuilder().getControlFlow(vertex)
        } catch (e: Exception) {
            logger.warn("Ignored method due to exception in control flow construction: ${e.message}")
            return
        }
        val instructions = controlFlow.instructions.toList()
        val elements = instructions.map { mapInstructionToPsiElement(it) }
        provideEdgeForMethodDeclaration(newEdges, elements)
        (instructions.indices).forEach { index ->
            provideEdgesForInstruction(index, instructions, elements, newEdges)
        }
    }

    private fun mapInstructionToPsiElement(instruction: Instruction): PsiElement? =
        when (instruction) {
            is GoBranchInstructionBase -> instruction.condition
            is GoForLoopStartInstruction -> instruction.forStatement
            is GoForLoopEndInstruction -> instruction.loopStartInstruction.forStatement
            else -> instruction.element
        }

    private fun provideEdgesForInstruction(
        index: Int,
        instructions: List<Instruction>,
        elements: List<PsiElement?>,
        newEdges: MutableSet<Edge>,
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
        if (instruction is GoReturnStatementInstruction || instruction.isTerminal()) {
            val methodRoot = element.parentOfType<GoFunctionOrMethodDeclaration>()
            if (methodRoot != null) {
                newEdges.add(Edge(element, methodRoot, EdgeType.ReturnsTo))
            }
        }
    }

    private fun provideEdgeForMethodDeclaration(newEdges: MutableSet<Edge>, elements: List<PsiElement?>) {
        val firstElement = elements.firstOrNull { it != null } ?: return
        val methodRoot = firstElement.parentOfType<GoFunctionOrMethodDeclaration>() ?: return
        newEdges.add(Edge(methodRoot, firstElement, EdgeType.ControlFlow))
    }

    private fun Instruction.isTerminal(): Boolean {
        return this.allSucc().all { mapInstructionToPsiElement(it) == null }
    }
}
