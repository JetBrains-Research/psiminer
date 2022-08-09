package psi.assignment

import com.intellij.psi.PsiElement

interface AssignmentProvider {

    fun getAllAssignments(root: PsiElement): List<PsiElement>

    fun getLeftPart(assignmentRoot: PsiElement): PsiElement?

    fun getRightPart(assignmentRoot: PsiElement): PsiElement?

    fun getLeftVariables(assignmentRoot: PsiElement): List<PsiElement>

    fun getRightVariables(assignmentRoot: PsiElement): List<PsiElement>
}
