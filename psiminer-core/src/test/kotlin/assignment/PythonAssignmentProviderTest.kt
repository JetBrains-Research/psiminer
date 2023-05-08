package assignment

import PythonPsiRequiredTest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.assignment.PythonAssignmentProvider

class PythonAssignmentProviderTest : PythonPsiRequiredTest("assignments") {

    private val pythonAssignmentProvider = PythonAssignmentProvider()

    @ParameterizedTest
    @ValueSource(
        strings = [
            "base",
            "multiple",
            "parallel",
            "augmented",
            "unpacking",
            "complex"
        ]
    )
    fun `test extraction of left side variables`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val leftSideVariables = pythonAssignmentProvider.getAllAssignments(psiRoot).map { assignmentRoot ->
            pythonAssignmentProvider.getLeftVariables(assignmentRoot).map { it.text }
        }
        assertEquals(correctLeftSideVariables[methodName], leftSideVariables)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "base",
            "multiple",
            "parallel",
            "augmented",
            "unpacking",
            "complex"
        ]
    )
    fun `test extraction of right side variables`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val rightSideVariables = pythonAssignmentProvider.getAllAssignments(psiRoot).map { assignmentRoot ->
            pythonAssignmentProvider.getRightVariables(assignmentRoot).map { it.text }
        }
        assertEquals(correctRightSideVariables[methodName], rightSideVariables)
    }

    companion object {
        val correctLeftSideVariables = mapOf(
            "base" to listOf(listOf("a")),
            "multiple" to listOf(listOf("a", "b")),
            "parallel" to listOf(listOf("a", "b")),
            "augmented" to listOf(listOf("a"), listOf("a")),
            "unpacking" to listOf(listOf("a", "b"), listOf("c", "d")),
            "complex" to listOf(listOf("a"))
        )
        val correctRightSideVariables = mapOf(
            "base" to listOf(emptyList()),
            "multiple" to listOf(emptyList()),
            "parallel" to listOf(listOf("c")),
            "augmented" to listOf(emptyList(), listOf("a")),
            "unpacking" to listOf(emptyList(), listOf("a", "b")),
            "complex" to listOf(listOf("augmented", "i", "i", "range", "n"))
        )
    }
}
