package psi.graphs.edgeProviders.go

import PythonPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import com.jetbrains.python.psi.PyAssignmentStatement
import com.jetbrains.python.psi.PyNamedParameter
import com.jetbrains.python.psi.PyTargetExpression
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.graphs.EdgeType
import psi.graphs.graphMiners.PythonGraphMiner

class PythonDeclarationUsageEdgeProviderTest : PythonPsiRequiredTest("decl_usage") {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "simple",
            "with_parameter",
            "with_if",
            "complex"
        ]
    )
    fun `test data flow extraction from Go methods`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = PythonGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot)
            val declarationUsageEdges = codeGraph.getAllEdges().filter {
                it.type == EdgeType.DeclarationUsage && !it.reversed
            }
            val textRepresentation = declarationUsageEdges.groupBy {
                when (val declaration = it.from.parent) {
                    is PyNamedParameter -> declaration.text
                    is PyTargetExpression -> when (declaration.parent) {
                        is PyAssignmentStatement -> declaration.parent.text
                        else -> declaration.text
                    }
                    else -> "unexpected variable declaration ${declaration.text}"
                }
            }.map { (varDecl, edges) ->
                varDecl to edges.size
            }.toMap()

            assertEquals(correctDeclCounts[methodName], textRepresentation)
        }
    }

    companion object {
        val correctDeclCounts: Map<String, Map<String, Int>> = mapOf(
            "simple" to mapOf(
                "a = 5" to 3,
                "b = a + a" to 1,
                "c = a + b" to 1
            ),
            "with_parameter" to mapOf(
                "a" to 3,
                "b = a + a" to 1
            ),
            "with_if" to mapOf(
                "a = 3" to 2,
                "b = 0" to 1,
                "b = 2" to 1
            ),
            "complex" to mapOf(
                "param" to 1,
                "a" to 3,
                "a = 4" to 1
            )
        )
    }
}
