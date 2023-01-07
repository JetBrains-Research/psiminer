package psi.graphs.edgeProviders.php

import PhpPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.impl.AssignmentExpressionImpl
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.graphs.Edge
import psi.graphs.EdgeType
import psi.graphs.graphMiners.PhpGraphMiner

internal class PhpDeclarationUsageEdgeProviderTest : PhpPsiRequiredTest("PhpFlowMethods") {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "straightWriteMethod",
            "straightReadWriteMethod",
            "ifMethod",
            "multipleDeclarations",
            "withParameter"
        ]
    )
    fun `test data flow extraction from PHP methods`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val graphMiner = PhpGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot)
            val declarationUsageEdges = codeGraph.getAllEdges().filter {
                it.type == EdgeType.DeclarationUsage && !it.reversed
            }
            val textRepresentation = declarationUsageEdges.groupBy {
                getTextRepresentation(it)
            }.map { (varDecl, edges) ->
                varDecl to edges.size
            }.toMap()
            assertEquals(correctDeclCounts[methodName], textRepresentation)
        }
    }

    private fun getTextRepresentation(edge: Edge): String {
        return when (val parent = edge.from.parent) {
            is AssignmentExpressionImpl -> parent.text
            is ParameterList -> edge.from.text
            else -> throw IllegalArgumentException("Declaration is neither first assignment or function parameter")
        }
    }

    companion object {
        val correctDeclCounts: Map<String, Map<String, Int>> = mapOf(
            "straightWriteMethod" to mapOf(
                "\$a = 0" to 1,
                "\$b = 1" to 1,
                "\$c = 2" to 1
            ),
            "straightReadWriteMethod" to mapOf(
                "\$a = 1" to 4,
                "\$b = \$a" to 3,
                "\$c = \$a + \$b" to 3,
                "\$d = \$c * \$c" to 1
            ),
            "ifMethod" to mapOf(
                "\$a = 1" to 3,
                "\$b = 2" to 1,
                "\$c = 3" to 1,
                "\$d = 4" to 1,
                "\$e = 5" to 1
            ),
            "multipleDeclarations" to mapOf(
                "\$i = 0" to 6
            ),
            "withParameter" to mapOf(
                "int \$a" to 3,
                "\$b = \$a + 1" to 2
            )
        )
    }
}
