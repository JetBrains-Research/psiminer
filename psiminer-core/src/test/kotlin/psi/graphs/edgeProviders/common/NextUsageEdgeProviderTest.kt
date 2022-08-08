package psi.graphs.edgeProviders.common

import JavaPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiElement
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.graphs.EdgeType
import psi.graphs.forward
import psi.graphs.graphMiners.JavaGraphMiner
import psi.graphs.withType
import psi.nodeProperties.nodeType

internal class NextUsageEdgeProviderTest : JavaPsiRequiredTest("JavaFlowMethods") {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "nestedIfs",
            "nestedFors",
            "multipleDeclarations",
            "nestedIfsTwoVariables",
        ]
    )
    fun `test next usage extraction from Java methods`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = JavaGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot)
            val nextUsageEdges = codeGraph.edges.withType(EdgeType.NextUsage).flatMap { (_, edges) -> edges.forward() }
            val textRepresentation = nextUsageEdges.map { Pair(getText(it.from), getText(it.to)) }.toSet()
            assertEquals(correctEdges[methodName], textRepresentation)
        }
    }

    companion object {
        val correctEdges: Map<String, Set<Pair<String, String>>> = mapOf(
            "nestedIfs" to setOf(
                Pair("int a = 1;", "a > 1"),
                Pair("a > 1", "a = 2"),
                Pair("a = 2", "a > 2"),
                Pair("a > 2", "a = 3"),
                Pair("a > 2", "a = 4"),
                Pair("a > 1", "a = 5"),
                Pair("a = 5", "a > 3"),
                Pair("a > 3", "a = 6"),
                Pair("a > 3", "a = 7"),
                Pair("a = 3", "a = 8"),
                Pair("a = 4", "a = 8"),
                Pair("a = 6", "a = 8"),
                Pair("a = 7", "a = 8"),
            ),
            "nestedFors" to setOf(
                Pair("int a = 1;", "a < 1"),
                Pair("a < 1", "a = 2"),
                Pair("a < 1", "a = 7"),
                Pair("a = 2", "a < 2"),
                Pair("a < 2", "a = 3"),
                Pair("a < 2", "a = 6"),
                Pair("a = 3", "a < 3"),
                Pair("a < 3", "a = 4"),
                Pair("a < 3", "a = 5"),
                Pair("a = 4", "a < 3"),
                Pair("a = 5", "a < 2"),
                Pair("a = 6", "a < 1"),
            ),
            "multipleDeclarations" to setOf(
                Pair("int a = 2;", "a <= 20"),
                Pair("a <= 20", "a *= 2"),
                Pair("a *= 2", "a++"),
                Pair("a++", "a <= 20"),
                Pair("int a = 3;", "a--"),
            ),
            "nestedIfsTwoVariables" to setOf(
                Pair("int b = 0;", "b > 1"),
                Pair("b > 1", "b > 2"),
                Pair("b > 1", "b > 3"),
                Pair("b > 2", "b = 9"),
                Pair("b > 3", "b = 9"),
                Pair("int a = 1;", "a = 2"),
                Pair("int a = 1;", "a = 5"),
                Pair("a = 2", "a = 3"),
                Pair("a = 2", "a = 4"),
                Pair("a = 5", "a = 6"),
                Pair("a = 5", "a = 7"),
                Pair("a = 3", "a = 8"),
                Pair("a = 4", "a = 8"),
                Pair("a = 6", "a = 8"),
                Pair("a = 7", "a = 8"),
            )
        )

        private fun getText(v: PsiElement) = if (v.nodeType == "IDENTIFIER") {
            v.parent.parent.text
        } else {
            v.text
        }
    }
}
