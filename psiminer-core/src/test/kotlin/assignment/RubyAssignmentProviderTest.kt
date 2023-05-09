package assignment

import RubyPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.assignment.RubyAssignmentProvider

class RubyAssignmentProviderTest : RubyPsiRequiredTest("assignments") {

    private val assignmentProvider = RubyAssignmentProvider()

    @ParameterizedTest
    @ValueSource(
        strings = [
            "simple",
            "abbreviated",
            "multiple",
            "with_func",
            "to_global",
            "initialize"
        ]
    )
    fun `test extraction of left side variables`(methodName: String) {
        ReadAction.run<Exception> {
            val psiRoot = getMethod(methodName)
            val leftSideVariables = assignmentProvider.getAllAssignments(psiRoot).map { assignmentRoot ->
                assignmentProvider.getLeftVariables(assignmentRoot).map { it.text }
            }
            assertEquals(correctLeftSideVariables[methodName], leftSideVariables)
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "simple",
            "abbreviated",
            "multiple",
            "with_func",
            "to_global",
            "initialize"
        ]
    )
    fun `test extraction of right side variables`(methodName: String) {
        ReadAction.run<Exception> {
            val psiRoot = getMethod(methodName)
            val rightSideVariables = assignmentProvider.getAllAssignments(psiRoot).map { assignmentRoot ->
                assignmentProvider.getRightVariables(assignmentRoot).map { it.text }
            }
            assertEquals(correctRightSideVariables[methodName], rightSideVariables)
        }
    }

    companion object {
        val correctLeftSideVariables = mapOf(
            "simple" to listOf(listOf("a"), listOf("b"), listOf("x"), listOf("y")),
            "abbreviated" to listOf(listOf("a"), listOf("a"), listOf("a")),
            "multiple" to listOf(listOf("a"), listOf("a", "b"), listOf("c", "d"), listOf("a", "b", "c", "d")),
            "with_func" to listOf(listOf("a"), listOf("b", "c")),
            "to_global" to listOf(listOf("local"), listOf("local2")),
            "initialize" to listOf(listOf("@cust_id"), listOf("@cust_name"), listOf("@cust_addr"),
                listOf("@@no_of_customers"))
        )
        val correctRightSideVariables = mapOf(
            "simple" to listOf(emptyList(), listOf("a"), listOf("y", "a"), listOf("a")),
            "abbreviated" to listOf(emptyList(), listOf("a"), listOf("a")),
            "multiple" to listOf(emptyList(), listOf("e", "e"), listOf("a", "b"), listOf("e", "e")),
            "with_func" to listOf(emptyList(), listOf("a", "multiple", "a")),
            "to_global" to listOf(listOf("\$global_variable"), listOf("local")),
            "initialize" to listOf(listOf("id"), listOf("name"), listOf("addr"), listOf("addr"))
        )
    }
}
