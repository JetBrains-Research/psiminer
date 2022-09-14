package psi.graphs.edgeProviders.java

import JavaPsiRequiredTest
import astminer.featureextraction.className
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.graphs.EdgeType
import psi.graphs.forward
import psi.graphs.graphMiners.JavaGraphMiner
import psi.graphs.withType

internal class JavaControlFlowEdgeProviderTest : JavaPsiRequiredTest("JavaFlowMethods") {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "straightWriteMethod",
            "ifMethod",
            "straightReadWriteMethod",
            "breakAndContinue",
            "forEach",
            "assertion",
        ]
    )
    fun `test control flow extraction from Java methods`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = JavaGraphMiner()
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

    @ParameterizedTest
    @ValueSource(
        strings = [
            "straightWriteMethod",
            "ifMethod",
            "straightReadWriteMethod",
            "breakAndContinue",
            "forEach",
            "assertion",
        ]
    )
    fun `test control element extraction from Java methods`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = JavaGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot)
            val controlElementEdges =
                codeGraph.edges.withType(EdgeType.ControlElement).flatMap { (_, edges) -> edges.forward() }
            val textRepresentation = controlElementEdges.map {
                Pair(it.from.shortText(), it.to.shortText())
            }.toSet()
            assertEquals(correctControlElementEdges[methodName], textRepresentation)
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "straightWriteMethod",
            "breakAndContinue",
            "multipleReturns",
        ]
    )
    fun `test returns to extraction from Java methods`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = JavaGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot)
            val returnsToEdges = codeGraph.edges.withType(EdgeType.ReturnsTo).flatMap { (_, edges) -> edges.forward() }
            val returnToMethodCount = returnsToEdges.count { it.to is PsiMethod }
            assertEquals(correctReturnToMethodCount[methodName], returnToMethodCount)
        }
    }

    companion object {
        val correctControlFlowEdges: Map<String, Set<Pair<String, String>>> = mapOf(
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
                Pair("a", "int b = 2;"),
                Pair("a", "a"), // if (a > 1) -> else if (a < 0),
                Pair("a", "int c = 3;"),
                Pair("a", "int d = 4;"),
                Pair("int b = 2;", "int e = 5;"),
                Pair("int c = 3;", "int e = 5;"),
                Pair("int d = 4;", "int e = 5;"),
            ),
            "breakAndContinue" to setOf(
                Pair("int j = 0;", "int k = 1;"),
                Pair("int k = 1;", "int i = 0;"),
                Pair("int i = 0;", "j"), // ... -> j < 10
                Pair("j", "int b = 2;"),
                Pair("j", "int e = 5;"),
                Pair("int b = 2;", "int e = 5;"),
                Pair("int c = 3;", "k"),
                Pair("k", "k++"), // read k and write back with k++
                Pair("k++", "j"),
            ),
            "forEach" to setOf(
                Pair("logs", "Disposable log"),
                Pair("Disposable log", "log"),
                Pair("log", "Disposer.dispose(log)"),
                Pair("Disposer.dispose(log)", "Disposable log"),
            ),
            "assertion" to setOf(
                Pair("int b = 1;", "a"),
                Pair("a", "b"),
                Pair("b", "int c = 2;"),
                Pair("int b = 1;", "int c = 2;"),
            )
        )

        val correctControlElementEdges: Map<String, Set<Pair<String, String>>> = mapOf(
            "straightWriteMethod" to emptySet(),
            "straightReadWriteMethod" to emptySet(),
            "ifMethod" to setOf(
                Pair("a", "if (a > 1) {...7"),
                Pair("if (a > 1) {...7", "a"), // ... -> if (a < 0)
                Pair("if (a > 1) {...7", "int b = 2;"),
                Pair("if (a > 1) {...7", "int e = 5;"),
                Pair("int b = 2;", "if (a > 1) {...7"),
                Pair("a", "if (a < 0) {...5"),
                Pair("if (a < 0) {...5", "int c = 3;"),
                Pair("if (a < 0) {...5", "int d = 4;"),
                Pair("if (a < 0) {...5", "int e = 5;"),
                Pair("int c = 3;", "if (a < 0) {...5"),
//                 ControlFlow instructions do not put END instruction after the last "else"
//                Pair("int d = 4;", "if (a < 0) {...5"),
            ),
            "breakAndContinue" to setOf(
                Pair("j", "for (int i = 0; j < 10; k++) {...14"), // j < 10 -> ...
                Pair("for (int i = 0; j < 10; k++) {...14", "int b = 2;"),
                Pair("for (int i = 0; j < 10; k++) {...14", "int e = 5;"),
                Pair("for (int i = 0; j < 10; k++) {...14", "j"),
                Pair("int b = 2;", "break;"),
                Pair("break;", "int e = 5;"),
                Pair("int c = 3;", "continue;"),
                Pair("continue;", "k"), // ... -> k++
                Pair("int d = 4;", "return;"),
                Pair("k++", "for (int i = 0; j < 10; k++) {...14"), // ... -> j < 10
            ),
            "forEach" to setOf(
                Pair("logs", "for (Disposable log : logs) {...3"),
                Pair("for (Disposable log : logs) {...3", "Disposable log"),
                Pair("Disposer.dispose(log)", "for (Disposable log : logs) {...3"),
            ),
            "assertion" to setOf(
                Pair("int b = 1;", "assert a == b;"),
                Pair("assert a == b;", "int c = 2;"),
                Pair("assert a == b;", "a"),
                Pair("b", "assert a == b;")
            )
        )

        val correctReturnToMethodCount: Map<String, Int> = mapOf(
            "breakAndContinue" to 2,
            "straightWriteMethod" to 1,
            "multipleReturns" to 3,
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
