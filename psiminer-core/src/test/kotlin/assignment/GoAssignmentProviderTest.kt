package assignment

import GoPsiRequiredTest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.assignment.GoAssignmentProvider

class GoAssignmentProviderTest : GoPsiRequiredTest("assignments") {

    private val goAssignmentProvider = GoAssignmentProvider()

    @ParameterizedTest
    @ValueSource(
        strings = [
            "simpleDeclarations",
            "constDeclarations",
            "shortDeclarations",
            "assignments",
            "lambda",
            "withParameter"
        ]
    )
    fun `test extraction of left side variables`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val leftSideVariables = goAssignmentProvider.getAllAssignments(psiRoot).map { assignmentRoot ->
            goAssignmentProvider.getLeftVariables(assignmentRoot).map { it.text }
        }
        assertEquals(correctLeftSideVariables[methodName], leftSideVariables)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "simpleDeclarations",
            "constDeclarations",
            "shortDeclarations",
            "assignments",
            "lambda",
            "withParameter"
        ]
    )
    fun `test extraction of right side variables`(methodName: String) {
        val psiRoot = getMethod(methodName)
        val rightSideVariables = goAssignmentProvider.getAllAssignments(psiRoot).map { assignmentRoot ->
            goAssignmentProvider.getRightVariables(assignmentRoot).map { it.text }
        }
        assertEquals(correctRightSideVariables[methodName], rightSideVariables)
    }

    companion object {
        val correctLeftSideVariables = mapOf(
            "simpleDeclarations" to listOf(listOf("a"), listOf("b"), listOf("c", "d", "e")),
            "constDeclarations" to listOf(listOf("a"), listOf("b")),
            "shortDeclarations" to listOf(listOf("a"), listOf("b", "c"), listOf("arr")),
            "assignments" to listOf(listOf("a", "b", "c"), listOf("a"), listOf("b", "c"), listOf("a")),
            "lambda" to listOf(listOf("f"), listOf("i")),
            "withParameter" to listOf(listOf("a"), listOf("b"))
        )
        val correctRightSideVariables = mapOf(
            "simpleDeclarations" to listOf(listOf(), listOf("a"), listOf("b", "b", "a", "a")),
            "constDeclarations" to listOf(listOf(), listOf("a", "a")),
            "shortDeclarations" to listOf(listOf(), listOf("a", "a"), listOf("a", "b")),
            "assignments" to listOf(listOf(), listOf(), listOf("a"), listOf()),
            "lambda" to listOf(listOf("i", "i"), listOf()),
            "withParameter" to listOf(listOf("param"), listOf("a", "param"))
        )
    }
}
