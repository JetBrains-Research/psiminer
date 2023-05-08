package psi.assignment

import IncorrectPsiTypeException
import astminer.featureextraction.className
import com.intellij.psi.*
import com.intellij.openapi.util.Condition
import com.jetbrains.php.lang.psi.elements.AssignmentExpression
import com.jetbrains.php.lang.psi.elements.MultiassignmentExpression
import com.jetbrains.php.lang.psi.elements.SelfAssignmentExpression
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl
import psi.preOrder

class PhpAssignmentProvider : AssignmentProvider {

    private val assignmentElements: List<Condition<PsiElement>> = listOf(
        AssignmentExpression.INSTANCEOF,
        MultiassignmentExpression.INSTANCEOF,
        SelfAssignmentExpression.INSTANCEOF
    )

    override fun getAllAssignments(root: PsiElement): List<PsiElement> =
        root.preOrder().filter { psiElement ->
            assignmentElements.any { condition ->
                condition.value(psiElement)
            }
        }

    private fun getLeftPart(assignmentRoot: PsiElement): PsiElement? =
        when (assignmentRoot) {
            is SelfAssignmentExpression -> assignmentRoot.variable
            is MultiassignmentExpression -> assignmentRoot.variable ?: assignmentRoot.firstChild
            is AssignmentExpression -> assignmentRoot.variable
            else -> throw IncorrectPsiTypeException("Value of class ${assignmentRoot.className()} passed as assignment")
        }

    private fun getRightPart(assignmentRoot: PsiElement): PsiElement? =
        when (assignmentRoot) {
            is AssignmentExpression -> assignmentRoot.value
            else -> throw IncorrectPsiTypeException("Value of class ${assignmentRoot.className()} passed as assignment")
        }

    override fun getLeftVariables(assignmentRoot: PsiElement): List<PsiElement> {
        val root = getLeftPart(assignmentRoot)
        return root?.preOrder()?.filterIsInstance<VariableImpl>() ?: emptyList()
    }

    override fun getRightVariables(assignmentRoot: PsiElement): List<PsiElement> {
        val root = getRightPart(assignmentRoot)
        return root?.preOrder()?.filterIsInstance<VariableImpl>() ?: emptyList()
    }
}
