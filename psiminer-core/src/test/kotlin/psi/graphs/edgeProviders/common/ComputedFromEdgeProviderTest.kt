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

internal class ComputedFromEdgeProviderTest : JavaPsiRequiredTest("JavaComputedFromMethods") {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "chainedVariables",
            "chainedAssignment",
            "multipleDeclarations",
            "plusAssignment",
        ]
    )
    fun `test computed from extraction from Java methods`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = JavaGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot)
            val computedFromEdges =
                codeGraph.edges.withType(EdgeType.ComputedFrom).flatMap { (_, edges) -> edges.forward() }
            val textRepresentation = computedFromEdges.map { Pair(getText(it.from), it.to.text) }.toSet()
            assertEquals(correctEdges[methodName], textRepresentation)
        }
    }

    companion object {
        val correctEdges: Map<String, Set<Pair<String, String>>> = mapOf(
            "chainedVariables" to setOf(
                Pair("b in int b = a;", "a"),
                Pair("c in int c = a + b;", "a"),
                Pair("c in int c = a + b;", "b"),
                Pair("d in int d = a + b + c;", "a"),
                Pair("d in int d = a + b + c;", "b"),
                Pair("d in int d = a + b + c;", "c"),
            ),
            "multipleDeclarations" to setOf(
                Pair("d in int d = a + b, e = c * c;", "a"),
                Pair("d in int d = a + b, e = c * c;", "b"),
                Pair("e in int d = a + b, e = c * c;", "c"),
            ),
            "chainedAssignment" to setOf(
                Pair("a in a = b = c = 1", "b"),
                Pair("a in a = b = c = 1", "c"),
                Pair("b in b = c = 1", "c"),
            ),
            "plusAssignment" to setOf(
                Pair("a in a += a + b", "a"),
                Pair("a in a += a + b", "b"),
            )
        )

        private fun getText(v: PsiElement) = if (v.nodeType == "IDENTIFIER") {
            "${v.text} in ${v.parent.parent.text}"
        } else {
            v.text
        }
    }
}
