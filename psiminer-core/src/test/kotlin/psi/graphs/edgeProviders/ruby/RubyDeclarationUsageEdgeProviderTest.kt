package psi.graphs.edgeProviders.ruby

import RubyPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RAssignmentExpression
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.graphs.EdgeType
import psi.graphs.graphMiners.RubyGraphMiner

class RubyDeclarationUsageEdgeProviderTest : RubyPsiRequiredTest("assignments") {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "simple",
            "abbreviated",
            "multiple",
            "to_global",
            "to_param"
        ]
    )
    fun `test data flow extraction from Go methods`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = RubyGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot)
            val declarationUsageEdges = codeGraph.getAllEdges().filter {
                it.type == EdgeType.DeclarationUsage && !it.reversed
            }
            val textRepresentation = declarationUsageEdges.groupBy {
                if (it.from.parent is RAssignmentExpression) {
                    it.from.parent.text
                } else {
                    it.from.text
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
                "a = 0" to 2,
                "b = a" to 1,
                "x = y = a" to 1,
                "y = a" to 1
            ),
            "abbreviated" to mapOf(
                "a = 1" to 1
            ),
            "multiple" to mapOf(
                "e" to 4,
                "a" to 1,
                "b" to 1
            ),
            "to_global" to mapOf(
                "local = \$global_variable" to 1
            ),
            "to_param" to mapOf(
                "a" to 2,
                "b = a" to 1
            )
        )
    }
}
