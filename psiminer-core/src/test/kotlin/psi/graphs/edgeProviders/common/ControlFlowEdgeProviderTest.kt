package psi.graphs.edgeProviders.common

import JavaPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.graphs.EdgeType
import psi.graphs.graphMiners.CommonGraphMiner

internal class ControlFlowEdgeProviderTest : JavaPsiRequiredTest("JavaFlowMethods") {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "straightWriteMethod",
            "ifMethod",
            "straightReadWriteMethod"
        ]
    )
    fun `test control flow extraction from Java methods`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = CommonGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot)
            val controlFlowEdges = codeGraph.getAllEdges().filter {
                it.type == EdgeType.ControlFlow && !it.reversed
            }
            val textRepresentation = controlFlowEdges.map {
                Pair(shortenText(it.from.text), shortenText(it.to.text))
            }.toSet()
            assertEquals(textRepresentation, correctEdges[methodName])
        }
    }

    companion object {
        val correctEdges: Map<String, Set<Pair<String, String>>> = mapOf(
            "straightWriteMethod" to setOf(
                Pair("int a = 1;", "int b = 2;"),
                Pair("int b = 2;", "int c = 3;"),
                Pair("int c = 3;", "int d = 4;"),
            ),
            "straightReadWriteMethod" to setOf(
                Pair("int a = 1;", "a"), // write a -> read a [int b = a;]
                Pair("a", "int b = a;"), // read a -> write b [int b = a;]
                Pair("int b = a;", "a"), // write b -> read a [b = 2 * a;]
                Pair("a", "b = 2 * a"), // read a -> write b [b = 2 * a;]
                Pair("b = 2 * a", "a"), // write b -> read a [int c = a + b;]
                Pair("a", "b"), // read a -> read b [int c = a + b;]
                Pair("b", "int c = a + b;"), // read b -> write c [int c = a + b;]
                Pair("int c = a + b;", "c"), // write c -> read c [int d = c * c;]
                Pair("c", "c"), // read c -> read c [int d = c * c;]
                Pair("c", "int d = c * c;"), // read c -> write d [int d = c * c;]
            ),
            "ifMethod" to setOf(
                Pair("int a = 1;", "a"),
                Pair("a", "if (a > 1) {...7"),
                Pair("if (a > 1) {...7", "int b = 2;"),
                Pair("if (a > 1) {...7", "a"), // ... -> if (a < 0)
                Pair("if (a > 1) {...7", "int e = 5;"), // GOTO at the end of scope
                Pair("int b = 2;", "if (a > 1) {...7"), // GOTO at the end of scope
                Pair("a", "if (a < 0) {...5"),
                Pair("if (a < 0) {...5", "int c = 3;"),
                Pair("if (a < 0) {...5", "int d = 4;"),
                Pair("if (a < 0) {...5", "int e = 5;"), // GOTO at the end of scope
                Pair("int c = 3;", "if (a < 0) {...5"), // GOTO at the end of scope
                Pair("int d = 4;", "int e = 5;"),
            )
        )

        private fun shortenText(text: String): String {
            val lines = text.lines()
            return if (lines.size <= 1) {
                text
            } else {
                lines[0] + "...${lines.size}"
            }
        }
    }
}
