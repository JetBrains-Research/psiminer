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
            "straightReadWriteMethod"
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
            assertEquals(correctControlFlowEdges[methodName], textRepresentation)
        }
    }

    companion object {

        val correctControlFlowEdges: Map<String, Set<Pair<String, String>>> = mapOf(
            "straightWriteMethod" to setOf(
                Pair("\$a = 0;", "\$a"), // statement a = 0 -> write a
                Pair("\$a", "\$b = 1;"), // write a -> statement b = 1
                Pair("\$b = 1;", "\$b"), // statement b = 1 -> write b
                Pair("\$b", "\$c = 2;"), // write b -> statement c = 2
                Pair("\$c = 2;", "\$c") // statement c = 2 -> write c
            ),
            "straightReadWriteMethod" to setOf(
                Pair("\$a = 0;", "\$a"),
                Pair("\$a", "\$b = \$a + 1;"),
                Pair("\$b = \$a + 1;", "\$a"),
                Pair("\$a", "\$b"),
                Pair("\$b", "\$c = \$a + \$b;"),
                Pair("\$c = \$a + \$b;", "\$a"),
                Pair("\$b", "\$c"),
                Pair("\$c", "\$d = \$c * \$c;"),
                Pair("\$d = \$c * \$c;", "\$c"),
                Pair("\$c", "\$c"),
                Pair("\$c", "\$d")
            )
        )

        private fun PsiElement.shortText(): String {
            val lines = text.lines()
            return if (lines.size <= 1) {
                text
            } else {
                lines[0] + "...${lines.size}"
            }
        }
    }
}
