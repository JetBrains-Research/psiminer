package psi.graphs.edgeProviders.java

import JavaPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.graphs.EdgeType
import psi.graphs.graphMiners.JavaGraphMiner

internal class JavaControlFlowEdgeProviderTest : JavaPsiRequiredTest("JavaFlowMethods") {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "straightWriteMethod",
            "ifMethod",
            "straightReadWriteMethod",
            "breakAndContinue",
        ]
    )
    fun `test control flow extraction from Java methods`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = JavaGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot)
            val controlFlowEdges = codeGraph.getAllEdges().filter {
                it.type == EdgeType.ControlFlow && !it.reversed
            }
            val textRepresentation = controlFlowEdges.map {
                Pair(shortenText(it.from.text), shortenText(it.to.text))
            }.toSet()
            assertEquals(correctEdges[methodName], textRepresentation)
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
                Pair("int b = 2;", "int e = 5;"),
                Pair("a", "if (a < 0) {...5"),
                Pair("if (a < 0) {...5", "int c = 3;"),
                Pair("if (a < 0) {...5", "int d = 4;"),
                Pair("int c = 3;", "int e = 5;"),
                Pair("int d = 4;", "int e = 5;"),
            ),
            "breakAndContinue" to setOf(
                Pair("int j = 0;", "int k = 1;"),
                Pair("int k = 1;", "int i = 0;"),
                Pair("int i = 0;", "j"), // ... -> j < 10
                Pair("j", "for (int i = 0; j < 10; k++) {...14"), // j < 10 -> ...
                Pair("for (int i = 0; j < 10; k++) {...14", "int b = 2;"),
                Pair("for (int i = 0; j < 10; k++) {...14", "int e = 5;"),
                Pair("int b = 2;", "int e = 5;"),
                Pair("int c = 3;", "k"), // ... -> k++
                Pair("k", "k++"), // read k and write back with k++
                Pair("k++", "j"), // ... -> j < 10
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
