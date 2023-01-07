package psi.assignment

import IncorrectPsiTypeException
import astminer.featureextraction.className
import com.intellij.psi.*
import com.jetbrains.php.lang.psi.elements.AssignmentExpression
import psi.preOrder

class PhpAssignmentProvider : AssignmentProvider {
    override fun getAllAssignments(root: PsiElement): List<PsiElement> =
        root.preOrder().flatMap { vertex ->
            when (vertex) {
                is AssignmentExpression-> listOf(vertex)
                else -> emptyList()
            }
        }

    override fun getLeftPart(assignmentRoot: PsiElement): PsiElement? =
        when (assignmentRoot) {
            is AssignmentExpression -> assignmentRoot.variable
            else -> throw IncorrectPsiTypeException("Value of class ${assignmentRoot.className()} passed as assignment")
        }

    override fun getRightPart(assignmentRoot: PsiElement): PsiElement? =
        when (assignmentRoot) {
            is AssignmentExpression -> assignmentRoot.value
            else -> throw IncorrectPsiTypeException("Value of class ${assignmentRoot.className()} passed as assignment")
        }

    override fun getLeftVariables(assignmentRoot: PsiElement): List<PsiElement> {
        val leftVariable = getLeftPart(assignmentRoot) ?: return emptyList()
        return listOf(leftVariable)
    }

    override fun getRightVariables(assignmentRoot: PsiElement): List<PsiElement> {
        val rightVariable = getRightPart(assignmentRoot) ?: return emptyList()
        return listOf(rightVariable)
    }
}
