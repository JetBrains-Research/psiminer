package psi.graphs.edgeProviders.go

import GoPsiRequiredTest
import com.goide.psi.GoParamDefinition
import com.goide.psi.GoVarOrConstDefinition
import com.intellij.openapi.application.ReadAction
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.graphs.EdgeType
import psi.graphs.graphMiners.GoGraphMiner

class GoDeclarationUsageEdgeProviderTest : GoPsiRequiredTest("assignments") {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "simpleDeclarations",
            "constDeclarations",
            "shortDeclarations",
            "lambda",
            "withParameter",
            "multipleDeclarations"
        ]
    )
    fun `test data flow extraction from Go methods`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = GoGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot)
            val declarationUsageEdges = codeGraph.getAllEdges().filter {
                it.type == EdgeType.DeclarationUsage && !it.reversed
            }
            val textRepresentation = declarationUsageEdges.groupBy {
                when (val declaredVariable = it.from) {
                    is GoVarOrConstDefinition -> "${declaredVariable.text} = ${declaredVariable.findExpression()?.text}"
                    is GoParamDefinition -> declaredVariable.parent.text
                    else -> "unexpected variable declaration ${declaredVariable.text}"
                }
            }.map { (varDecl, edges) ->
                varDecl to edges.size
            }.toMap()

            assertEquals(correctDeclCounts[methodName], textRepresentation)
        }
    }

    companion object {
        val correctDeclCounts: Map<String, Map<String, Int>> = mapOf(
            "simpleDeclarations" to mapOf(
                "a = 0" to 4,
                "b = a" to 3,
                "c = b" to 1,
                "d = b * 2" to 1,
                "e = a + a" to 1
            ),
            "constDeclarations" to mapOf(
                "a = 0" to 3,
                "b = a + a" to 1
            ),
            "shortDeclarations" to mapOf(
                "a = 0" to 4,
                "b = a + 1" to 2,
                "c = a + 2" to 1,
                "arr = [2]int{a, b}" to 1
            ),
            "lambda" to mapOf(
                "f = func() int {\n" +
                        "\t\ti := 0\n" +
                        "\t\treturn i + 1\n" +
                        "\t}" to 2,
                "i = 0" to 2
            ),
            "withParameter" to mapOf(
                "param int" to 3,
                "a = param" to 2,
                "b = a + param" to 1
            ),
            "multipleDeclarations" to mapOf(
                "a = 0" to 2,
                "a = 1" to 2,
                "b = a + 1" to 1,
                "a = 2" to 4,
                "b = a" to 1
            )
        )
    }
}
