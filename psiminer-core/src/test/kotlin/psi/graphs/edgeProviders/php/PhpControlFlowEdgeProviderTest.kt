package psi.graphs.edgeProviders.php

import com.intellij.openapi.application.ReadAction
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.graphs.*
import psi.graphs.graphMiners.PhpGraphMiner

internal class PhpControlFlowEdgeProviderTest : PhpGraphTest("PhpFlowMethods") {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "straightWriteMethod",
            "ifMethod",
            "forMethod"
        ]
    )
    fun `test control flow extraction from PHP methods`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = PhpGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot)
            val controlFlowEdges =
                codeGraph.edges.withType(EdgeType.ControlFlow).flatMap { (_, edges) -> edges.forward() }
            assertContainsElements(
                countIncomingEdges(controlFlowEdges).entries,
                correctNumberOfControlFlowEdges.incoming[methodName]?.entries
                    ?: throw CorrectValueNotProvidedException(methodName, "control flow")
            )
            assertContainsElements(
                countOutgoingEdges(controlFlowEdges).entries,
                correctNumberOfControlFlowEdges.outgoing[methodName]?.entries
                    ?: throw CorrectValueNotProvidedException(methodName, "control flow")
            )
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "straightWriteMethod",
            "ifMethod",
            "forMethod"
        ]
    )
    fun `test control element extraction from PHP methods`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = PhpGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot)
            val controlFlowEdges =
                codeGraph.edges.withType(EdgeType.ControlElement).flatMap { (_, edges) -> edges.forward() }
            assertContainsElements(
                countIncomingEdges(controlFlowEdges).entries,
                correctNumberOfControlElementEdges.incoming[methodName]?.entries
                    ?: throw CorrectValueNotProvidedException(methodName, "control element")
            )
            assertContainsElements(
                countOutgoingEdges(controlFlowEdges).entries,
                correctNumberOfControlElementEdges.outgoing[methodName]?.entries
                    ?: throw CorrectValueNotProvidedException(methodName, "control element")
            )
        }
    }

    companion object {

        val correctNumberOfControlFlowEdges = CorrectNumberOfIncomingAndOutgoingEdges(
            incoming = mapOf(
                "straightWriteMethod" to mapOf(
                    Vertex("VariableImpl: a", Pair(2, 8)) to 1,
                    Vertex("VariableImpl: b", Pair(3, 8)) to 1,
                    Vertex("VariableImpl: c", Pair(4, 8)) to 1
                ),
                "ifMethod" to mapOf(
                    Vertex("BinaryExpressionImpl: \$a > 1", Pair(3, 12)) to 1,
                    Vertex("VariableImpl: b", Pair(4, 12)) to 1,
                    Vertex("Statement", Pair(10, 8)) to 3
                ),
                "forMethod" to mapOf(
                    Vertex("VariableImpl: i", Pair(2, 13)) to 1,
                    Vertex("VariableImpl: i", Pair(2, 21)) to 2,
                    Vertex("Break", Pair(4, 16)) to 1
                )
            ),
            outgoing = mapOf(
                "straightWriteMethod" to mapOf(
                    Vertex("MethodImpl: straightWriteMethod", Pair(0, 20)) to 1,
                    Vertex("VariableImpl: a", Pair(2, 8)) to 1,
                    Vertex("VariableImpl: b", Pair(3, 8)) to 1
                ),
                "ifMethod" to mapOf(
                    Vertex("BinaryExpressionImpl: \$a > 1", Pair(3, 12)) to 2,
                    Vertex("BinaryExpressionImpl: \$a < 0", Pair(5, 19)) to 2,
                    Vertex("VariableImpl: c", Pair(6, 12)) to 1
                ),
                "forMethod" to mapOf(
                    Vertex("VariableImpl: i", Pair(2, 13)) to 1,
                    Vertex("BinaryExpressionImpl: \$i == 1", Pair(3, 16)) to 2
                )
            )
        )

        val correctNumberOfControlElementEdges = CorrectNumberOfIncomingAndOutgoingEdges(
            incoming = mapOf(
                "straightWriteMethod" to mapOf(),
                "ifMethod" to mapOf(
                    Vertex("If", Pair(3, 8)) to 1, // $a = 1 -> if ($a > 1)...
                    Vertex("VariableImpl: a", Pair(3, 12)) to 1, // if ($a > 1)... -> $a
                    Vertex("If", Pair(5, 15)) to 1, // if ($a > 1)... -> else if ($a < 0)...
                    Vertex("VariableImpl: a", Pair(5, 19)) to 1 // else if ($a < 0)... -> $a
                ),
                "forMethod" to mapOf(
                    Vertex("For", Pair(2, 8)) to 3, // 1. forMethod -> for ()  2. $i -> for()  3. break -> for()
                    Vertex("VariableImpl: i", Pair(2, 13)) to 1, // for () -> $i = 0
                    Vertex("BinaryExpressionImpl: \$i < 2", Pair(2, 21)) to 1 // for () -> $i < 2
                )
            ),
            outgoing = mapOf(
                "straightWriteMethod" to mapOf(),
                "ifMethod" to mapOf(
                    Vertex("VariableImpl: a", Pair(2, 8)) to 1, // $a = 1 -> if ($a > 1)...
                    Vertex("If", Pair(3, 8)) to 1 // if ($a > 1)... -> $a
                ),
                "forMethod" to mapOf(
                    Vertex("MethodImpl: forMethod", Pair(0, 20)) to 1, // forMethod -> for()
                    Vertex("For", Pair(2, 8)) to 2, // 1. for() -> $i = 0  2. for() -> $i < 2
                    Vertex("VariableImpl: i", Pair(2, 21)) to 1, // $i -> $i < 2
                    Vertex("Break", Pair(4, 16)) to 1 // break -> for()
                )
            )
        )
    }
}
