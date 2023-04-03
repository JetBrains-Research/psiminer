package psi.assignment

import com.intellij.psi.PsiElement

class KotlinAssignmentProvider : AssignmentProvider {
    override fun getAllAssignments(root: PsiElement): List<PsiElement> {
        TODO("Not yet implemented")
    }

    override fun getLeftVariables(assignmentRoot: PsiElement): List<PsiElement> {
        TODO("Not yet implemented")
    }

    override fun getRightVariables(assignmentRoot: PsiElement): List<PsiElement> {
        TODO("Not yet implemented")
    }
}
