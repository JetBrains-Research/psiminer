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
            "ifMethod"
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
            //println(textRepresentation)
            assertEquals(correctControlFlowEdges[methodName], textRepresentation)
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "straightWriteMethod",
            "straightReadWriteMethod",
            "ifMethod",
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
            //println(textRepresentation)
            assertEquals(correctControlElementEdges[methodName], textRepresentation)
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
                Pair("\$a = 1;", "\$a"), // statement a = 0 -> write a [a = 1]
                Pair("\$a", "\$b = \$a;"), // write a -> statement b = a [b = a]
                Pair("\$b = \$a;", "\$a"), // statement b = a -> read a [b = a]
                Pair("\$a", "\$b"),  // read a -> write b [b = a]
                Pair("\$b", "\$b = 2 * \$a;"), // write b -> statement b = 2 * a [b = 2 * a]
                Pair("\$b = 2 * \$a;", "\$a"), // statement b = 2 * a -> read a
                Pair("\$a", "\$b"), // read -> write b
                Pair("\$b", "\$c = \$a + \$b;"), // write b -> statement c = a + b [c = a + b]
                Pair("\$c = \$a + \$b;", "\$a"), // statement c = a + b -> read a [c = a + b]
                Pair("\$a", "\$b"), // read a -> read b [c = a + b]
                Pair("\$b", "\$c"), // read b -> write c [c = a + b]
                Pair("\$c", "\$d = \$c * \$c;"), // write c -> statement d = c * c [d = c * c]
                Pair("\$d = \$c * \$c;", "\$c"), // statement d = c * c -> read c [d = c * c]
                Pair("\$c", "\$c"), // read c -> read c [d = c * c]
                Pair("\$c", "\$d") // read c -> write d [d = c * c]
            ),
            "ifMethod" to setOf(
                Pair("\$a = 1;", "\$a"), // statement a = 1 -> write a
                Pair("\$a", "if (\$a > 1) {...7"), // write a -> statement if...
                Pair("if (\$a > 1) {...7", "\$a"), // statement if... -> read a
                Pair("\$a", "\$b = 2;"), // read a -> statement b = 2
                Pair("\$a", "if (\$a < 0) {...5"), // write a -> statement if...
                Pair("\$b = 2;", "\$b"), // statement b = 2 -> write b
                Pair("if (\$a < 0) {...5", "\$a"), // statement if... -> read a
                Pair("\$b", "\$e = 5;"), // write b -> statement e = 5
                Pair("\$e = 5;", "\$e"), // statement e = 5 -> write e
                Pair("\$a", "\$c = 3;"), // write a -> statement c = 3
                Pair("\$a", "\$d = 4;"), // write a -> statement d = 4
                Pair("\$c = 3;", "\$c"), // statement c = 3 -> write c
                Pair("\$d = 4;", "\$d"), // statement d = 4 -> write d
                Pair("\$c", "\$e = 5;"), // write c -> statement e = 5
                Pair("\$d", "\$e = 5;") // write d -> statement e = 5
            )
        )

        val correctControlElementEdges: Map<String, Set<Pair<String, String>>> = mapOf(
            "straightWriteMethod" to emptySet(),
            "straightReadWriteMethod" to emptySet(),
            "ifMethod" to setOf(
                Pair("\$a", "\$a > 1"),
                Pair("\$a > 1", "\$b = 2;"),
                Pair("\$a > 1", "if (\$a < 0) {...5"),
                Pair("\$a", "\$a < 0"),
                Pair("\$a < 0", "\$c = 3;"),
                Pair("\$a < 0", "\$d = 4;")
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
