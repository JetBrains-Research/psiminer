package psi.graphs.edgeProviders.java

import JavaPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.jupiter.api.Test
import psi.graphs.EdgeType
import psi.graphs.graphMiners.JavaGraphMiner

internal class JavaMethodUsageEdgeProviderTest : JavaPsiRequiredTest("JavaMethodCalls") {

    @Test
    fun `test method usage edge extraction from Java files`() {
        val psiRoot = psiFile
        assertNotNull(psiRoot)
        val graphMiner = JavaGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot!!)
            val methodUsageEdges = codeGraph.getAllEdges().filter {
                it.type == EdgeType.MethodDeclarationUsage && !it.reversed
            }
            methodUsageEdges.forEach {
                println("${it.from.text} to ${it.to.text}")
            }
        }
    }

    @Test
    fun `test argument to parameter edge extraction from Java files`() {
        val psiRoot = psiFile
        assertNotNull(psiRoot)
        val graphMiner = JavaGraphMiner()
        ReadAction.run<Exception> {
            val codeGraph = graphMiner.mine(psiRoot!!)
            val methodUsageEdges = codeGraph.getAllEdges().filter {
                it.type == EdgeType.ArgumentToParameter && !it.reversed
            }
            methodUsageEdges.forEach {
                println("${it.from.text} to ${it.to.text}")
            }
        }
    }
}
