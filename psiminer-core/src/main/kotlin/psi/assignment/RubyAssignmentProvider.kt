package psi.assignment

import IncorrectPsiTypeException
import astminer.featureextraction.className
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RAssignmentExpression
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RIdentifier
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RVariable
import psi.preOrder

class RubyAssignmentProvider : AssignmentProvider {

    override fun getAllAssignments(root: PsiElement): List<PsiElement> =
        root.preOrder().filterIsInstance<RAssignmentExpression>()

    private fun getLeftPart(assignmentRoot: PsiElement): PsiElement =
        if (assignmentRoot is RAssignmentExpression) {
            assignmentRoot.`object`
        } else {
            throw IncorrectPsiTypeException("Value of class ${assignmentRoot.className()} passed as assignment")
        }

    private fun getRightPart(assignmentRoot: PsiElement): PsiElement? =
        if (assignmentRoot is RAssignmentExpression) {
            assignmentRoot.value
        } else {
            throw IncorrectPsiTypeException("Value of class ${assignmentRoot.className()} passed as assignment")
        }

    override fun getLeftVariables(assignmentRoot: PsiElement): List<PsiElement> {
        val root = getLeftPart(assignmentRoot)
        return root.preOrder().filter {
            it is RIdentifier || it is RVariable
        }
    }

    override fun getRightVariables(assignmentRoot: PsiElement): List<PsiElement> {
        val root = getRightPart(assignmentRoot)
        return root?.preOrder()?.filter {
            it is RIdentifier || it is RVariable
        } ?: emptyList()
    }
}
