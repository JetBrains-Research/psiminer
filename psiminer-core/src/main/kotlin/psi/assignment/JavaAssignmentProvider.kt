package psi.assignment

import IncorrectPsiTypeException
import astminer.featureextraction.className
import com.intellij.psi.PsiAssignmentExpression
import com.intellij.psi.PsiDeclarationStatement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiLocalVariable
import psi.preOrder

class JavaAssignmentProvider : AssignmentProvider {

    override fun getAllAssignments(root: PsiElement): List<PsiElement> =
        root.preOrder().flatMap { vertex ->
            when (vertex) {
                is PsiAssignmentExpression -> listOf(vertex)
                is PsiDeclarationStatement -> vertex.declaredElements.toList().filterIsInstance<PsiLocalVariable>()
                else -> emptyList()
            }
        }

    private fun getLeftPart(assignmentRoot: PsiElement): PsiElement? =
        when (assignmentRoot) {
            is PsiAssignmentExpression -> assignmentRoot.lExpression
            is PsiLocalVariable -> assignmentRoot.nameIdentifier
            else -> throw IncorrectPsiTypeException("Value of class ${assignmentRoot.className()} passed as assignment")
        }

    private fun getRightPart(assignmentRoot: PsiElement): PsiElement? =
        when (assignmentRoot) {
            is PsiAssignmentExpression -> assignmentRoot.rExpression
            is PsiLocalVariable -> assignmentRoot.initializer
            else -> throw IncorrectPsiTypeException("Value of class ${assignmentRoot.className()} passed as assignment")
        }

    override fun getLeftVariables(assignmentRoot: PsiElement): List<PsiElement> {
        val root = getLeftPart(assignmentRoot)
        return root?.preOrder()?.filterIsInstance<PsiIdentifier>() ?: emptyList()
    }

    override fun getRightVariables(assignmentRoot: PsiElement): List<PsiElement> {
        val root = getRightPart(assignmentRoot)
        return root?.preOrder()?.filterIsInstance<PsiIdentifier>() ?: emptyList()
    }
}
