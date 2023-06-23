package psi.graphs.edgeProviders.go

import PythonPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import com.jetbrains.python.psi.PyFunction
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.graphs.EdgeType
import psi.graphs.graphMiners.PythonGraphMiner

class PythonCallDeclarationEdgeProviderTest : PythonPsiRequiredTest("function_in_function") {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "factorial",
            "triple_nested1",
            "to_outside"
        ]
    )
    fun `test call and declaration have same name`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = PythonGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot)
            assertTrue(
                codeGraph.getAllEdges().filter {
                    it.type == EdgeType.CallDeclaration && !it.reversed
                }.all { edge ->
                    edge.from.text == (edge.to as PyFunction).name
                }
            )
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "factorial",
            "triple_nested1",
            "to_outside"
        ]
    )
    fun `test number of calls`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = PythonGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot)
            val callsCount = codeGraph.getAllEdges().filter {
                it.type == EdgeType.CallDeclaration && !it.reversed
            }.groupBy { it.to }
                .mapKeys { (it.key as PyFunction).name }
                .mapValues { it.value.size }
            assertEquals(correctCallsCount[methodName], callsCount)
        }
    }

    companion object {
        val correctCallsCount = mapOf(
            "factorial" to mapOf(
                "inner_factorial" to 2
            ),
            "triple_nested1" to mapOf(
                "triple_nested1" to 2,
                "triple_nested2" to 2,
                "triple_nested3" to 1
            ),
            "to_outside" to mapOf(
                "to_outside" to 1
            )
        )
    }
}
