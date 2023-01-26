package psi.graphs.edgeProviders.php

import PhpPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiElement
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.graphs.EdgeType
import psi.graphs.forward
import psi.graphs.graphMiners.PhpGraphMiner
import psi.graphs.withType

internal class PhpControlFlowEdgeProviderTest : PhpPsiRequiredTest("PhpFlowMethods") {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "straightWriteMethod",
            "straightReadWriteMethod",
            "ifMethod",
            "forMethod",
            "foreachMethod",
            "multipleReturns"
        ]
    )
    fun `test control flow extraction from PHP methods`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = PhpGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot)
            val controlFlowEdges =
                codeGraph.edges.withType(EdgeType.ControlFlow).flatMap { (_, edges) -> edges.forward() }
            val textRepresentation = controlFlowEdges.map {
                Pair(it.from.shortText(), it.to.shortText())
            }.toSet()
            println(textRepresentation)
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "straightWriteMethod",
            "straightReadWriteMethod",
            "ifMethod",
            "forMethod",
            "foreachMethod",
            "multipleReturns"
        ]
    )
    fun `test control element extraction from PHP methods`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = PhpGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot)
            val controlElementEdges =
                codeGraph.edges.withType(EdgeType.ControlElement).flatMap { (_, edges) -> edges.forward() }
            val textRepresentation = controlElementEdges.map {
                Pair(it.from.shortText(), it.to.shortText())
            }.toSet()
            println(textRepresentation)
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "straightWriteMethod",
            "straightReadWriteMethod",
            "ifMethod",
            "forMethod",
            "foreachMethod",
            "multipleReturns"
        ]
    )
    fun `test return element extraction from PHP methods`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = PhpGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot)
            val controlElementEdges =
                codeGraph.edges.withType(EdgeType.ReturnsTo).flatMap { (_, edges) -> edges.forward() }
            val textRepresentation = controlElementEdges.map {
                Pair(it.from.shortText(), it.to.shortText())
            }.toSet()
            println(textRepresentation)
        }
    }

    private fun PsiElement.shortText(): String {
        val lines = text.lines()
        return if (lines.size <= 1) {
            text
        } else {
            lines[0] + "...${lines.size}"
        }
    }
}
