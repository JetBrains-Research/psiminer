package psi.graphs.edgeProviders.php

import PhpPsiRequiredTest
import com.intellij.openapi.application.ReadAction
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
            assertTrue(
                countIncomingEdges(controlFlowEdges).containsAll(
                    correctNumberOfControlFlowEdges.incoming[methodName]
                )
            )
            assertTrue(
                countOutgoingEdges(controlFlowEdges).containsAll(
                    correctNumberOfControlFlowEdges.outgoing[methodName]
                )
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
            println("Incoming: ${countIncomingEdges(controlFlowEdges)}")
            println("Outgoing: ${countOutgoingEdges(controlFlowEdges)}")
            assertTrue(
                countIncomingEdges(controlFlowEdges).containsAll(
                    correctNumberOfControlElementEdges.incoming[methodName]
                )
            )
            assertTrue(
                countOutgoingEdges(controlFlowEdges).containsAll(
                    correctNumberOfControlElementEdges.outgoing[methodName]
                )
            )
        }
    }

    companion object {

        val correctNumberOfControlFlowEdges = CorrectNumberOfIncomingAndOutgoingEdges(
            incoming = mapOf(
                "straightWriteMethod" to mapOf(
                    Vertex("VariableImpl: a", Pair(9, 9)) to 1,
                    Vertex("VariableImpl: b", Pair(10, 9)) to 1,
                    Vertex("VariableImpl: c", Pair(11, 9)) to 1
                ),
                "ifMethod" to mapOf(
                    Vertex("BinaryExpressionImpl: \$a > 1", Pair(26, 13)) to 1,
                    Vertex("VariableImpl: b", Pair(27, 13)) to 1,
                    Vertex("Statement", Pair(33, 9)) to 3
                ),
                "forMethod" to mapOf(
                    Vertex("VariableImpl: i", Pair(57, 14)) to 1,
                    Vertex("VariableImpl: i", Pair(57, 22)) to 2,
                    Vertex("Break", Pair(59, 17)) to 1
                )
            ),
            outgoing = mapOf(
                "straightWriteMethod" to mapOf(
                    Vertex("MethodImpl: straightWriteMethod", Pair(7, 21)) to 1,
                    Vertex("VariableImpl: a", Pair(9, 9)) to 1,
                    Vertex("VariableImpl: b", Pair(10, 9)) to 1
                ),
                "ifMethod" to mapOf(
                    Vertex("BinaryExpressionImpl: \$a > 1", Pair(26, 13)) to 2,
                    Vertex("BinaryExpressionImpl: \$a < 0", Pair(28, 20)) to 2,
                    Vertex("VariableImpl: c", Pair(29, 13)) to 1
                ),
                "forMethod" to mapOf(
                    Vertex("VariableImpl: i", Pair(57, 14)) to 1,
                    Vertex("BinaryExpressionImpl: \$i == 1", Pair(58, 17)) to 2
                )
            )
        )

        val correctNumberOfControlElementEdges = CorrectNumberOfIncomingAndOutgoingEdges(
            incoming = mapOf(
                "straightWriteMethod" to mapOf(),
                "ifMethod" to mapOf(
                    Vertex("If", Pair(26, 9)) to 1, // $a = 1 -> if ($a > 1)...
                    Vertex("VariableImpl: a", Pair(26, 13)) to 1, // if ($a > 1)... -> $a
                    Vertex("If", Pair(28, 16)) to 1, // if ($a > 1)... -> else if ($a < 0)...
                    Vertex("VariableImpl: a", Pair(28, 20)) to 1 // else if ($a < 0)... -> $a
                ),
                "forMethod" to mapOf(
                    Vertex("For", Pair(57, 9)) to 3, // 1. forMethod -> for ()  2. $i -> for()  3. break -> for()
                    Vertex("VariableImpl: i", Pair(57, 14)) to 1, // for () -> $i = 0
                    Vertex("BinaryExpressionImpl: \$i < 2", Pair(57, 22)) to 1 // for () -> $i < 2
                )
            ),
            outgoing = mapOf(
                "straightWriteMethod" to mapOf(),
                "ifMethod" to mapOf(
                    Vertex("VariableImpl: a", Pair(25, 9)) to 1, // $a = 1 -> if ($a > 1)...
                    Vertex("If", Pair(26, 9)) to 1 // if ($a > 1)... -> $a
                ),
                "forMethod" to mapOf(
                    Vertex("MethodImpl: forMethod", Pair(55, 21)) to 1, // forMethod -> for()
                    Vertex("For", Pair(57, 9)) to 2, // 1. for() -> $i = 0  2. for() -> $i < 2
                    Vertex("VariableImpl: i", Pair(57, 22)) to 1, // $i -> $i < 2
                    Vertex("Break", Pair(59, 17)) to 1 // break -> for()
                )
            )
        )
    }
}
