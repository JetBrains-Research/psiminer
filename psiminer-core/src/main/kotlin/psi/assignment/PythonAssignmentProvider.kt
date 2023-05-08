package psi.assignment

import IncorrectPsiTypeException
import astminer.featureextraction.className
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.jetbrains.python.psi.PyAssignmentStatement
import com.jetbrains.python.psi.PyAugAssignmentStatement
import com.jetbrains.python.psi.PyReferenceExpression
import com.jetbrains.python.psi.PyTargetExpression
import psi.preOrder

class PythonAssignmentProvider : AssignmentProvider {

    override fun getAllAssignments(root: PsiElement): List<PsiElement> =
        root.preOrder().filter {
            it is PyAssignmentStatement || it is PyAugAssignmentStatement
        }

    private fun getLeftPart(assignmentRoot: PsiElement): List<PsiElement> =
        when (assignmentRoot) {
            is PyAssignmentStatement -> assignmentRoot.targets.toList()
            is PyAugAssignmentStatement -> listOf(assignmentRoot.target)
            else -> throw IncorrectPsiTypeException("Value of class ${assignmentRoot.className()} passed as assignment")
        }

    private fun getRightPart(assignmentRoot: PsiElement): PsiElement? =
        when (assignmentRoot) {
            is PyAssignmentStatement -> assignmentRoot.assignedValue
            is PyAugAssignmentStatement -> assignmentRoot.value
            else -> throw IncorrectPsiTypeException("Value of class ${assignmentRoot.className()} passed as assignment")
        }

    override fun getLeftVariables(assignmentRoot: PsiElement): List<PsiElement> =
        getLeftPart(assignmentRoot).flatMap { root ->
            root.preOrder().filterIsInstance<LeafPsiElement>()
        }

    override fun getRightVariables(assignmentRoot: PsiElement): List<PsiElement> {
        return getRightPart(assignmentRoot)
            ?.preOrder()
            ?.filter { it is PyReferenceExpression || it is PyTargetExpression }
            ?.map { it.firstChild } ?: emptyList()
    }
}
