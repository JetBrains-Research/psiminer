package assignment

import PhpPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.assignment.PhpAssignmentProvider

internal class PhpAssignmentProviderTest : PhpPsiRequiredTest("PhpAssignmentMethods") {

    private val phpAssignmentProvider = PhpAssignmentProvider()

    @ParameterizedTest
    @ValueSource(
        strings = [
            "straightAssignments",
            "multiAssignment",
            "selfAssignment"
        ]
    )
    fun `test all assignments extraction`(methodName: String) = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        assertEquals(correctNumberOfAssignments[methodName], phpAssignmentProvider.getAllAssignments(psiRoot).size)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "straightAssignments",
            "multiAssignment",
            "selfAssignment"
        ]
    )
    fun `test extraction of all left part variables`(methodName: String) = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        val variablesInLeftParts = phpAssignmentProvider.getAllAssignments(psiRoot).map { assignmentRoot ->
            phpAssignmentProvider.getLeftVariables(assignmentRoot).map { it.text }
        }
        assertEquals(correctVariablesInLeftParts[methodName], variablesInLeftParts)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "straightAssignments",
            "multiAssignment",
            "selfAssignment"
        ]
    )
    fun `test extraction of all right part variables`(methodName: String) = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        val variablesInRightParts = phpAssignmentProvider.getAllAssignments(psiRoot).map { assignmentRoot ->
            phpAssignmentProvider.getRightVariables(assignmentRoot).map { it.text }
        }
        assertEquals(correctVariablesInRightParts[methodName], variablesInRightParts)
    }

    companion object {
        val correctNumberOfAssignments = mapOf(
            "straightAssignments" to 3,
            "multiAssignment" to 3,
            "selfAssignment" to 2
        )
        val correctLeftParts = mapOf(
            "straightAssignments" to listOf("\$a", "\$b", "\$c"),
            "multiAssignment" to listOf("\$a", "\$b", "[\$c, \$d]"),
            "selfAssignment" to listOf("\$a", "\$a")
        )
        val correctRightParts = mapOf(
            "straightAssignments" to listOf("0", "\$c = array()", "array()"),
            "multiAssignment" to listOf("\$b = \"ab\"", "\"ab\"", "array(\$a, \$b)"),
            "selfAssignment" to listOf("0", "4")
        )
        val correctVariablesInLeftParts = mapOf(
            "straightAssignments" to listOf(listOf("\$a"), listOf("\$b"), listOf("\$c")),
            "multiAssignment" to listOf(
                listOf("\$a"),
                listOf("\$b"),
                listOf("\$c", "\$d")
            ),
            "selfAssignment" to listOf(listOf("\$a"), listOf("\$a"))
        )
        val correctVariablesInRightParts = mapOf(
            "straightAssignments" to listOf(emptyList(), listOf("\$c"), emptyList()),
            "multiAssignment" to listOf(
                listOf("\$b"),
                emptyList(),
                listOf("\$a", "\$b")
            ),
            "selfAssignment" to listOf(emptyList(), emptyList())
        )
    }
}
