package psi.assignment

import IncorrectPsiTypeException
import astminer.featureextraction.className
import com.goide.psi.*
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import psi.preOrder

class GoAssignmentProvider : AssignmentProvider {
    override fun getAllAssignments(root: PsiElement): List<PsiElement> =
        root.preOrder().flatMap { vertex ->
            when (vertex) {
                is GoAssignmentStatement -> listOf(vertex)
                is GoShortVarDeclaration -> listOf(vertex) // for declarations using ':='
                is GoVarOrConstDeclaration<*> -> vertex.specList // for declarations using '='
                else -> emptyList()
            }
        }

    private fun PsiElement.findVariablesInSubtree(): List<PsiElement> =
        ReadAction.compute<List<PsiElement>, Exception> {
            this.preOrder().filterIsInstance<LeafPsiElement>().filter { vertex ->
                val parent = vertex.parent
                val declaration = if (parent is GoReferenceExpression) {
                    parent.reference.resolve()
                } else {
                    parent
                }
                declaration is GoVarOrConstDefinition || declaration is GoParamDefinition
            }
        }

    override fun getLeftVariables(assignmentRoot: PsiElement): List<PsiElement> =
        when (assignmentRoot) {
            is GoAssignmentStatement -> assignmentRoot.leftHandExprList.findVariablesInSubtree()
            is GoShortVarDeclaration -> assignmentRoot.definitionList.flatMap { it.findVariablesInSubtree() }
            is GoVarOrConstSpec<*> -> assignmentRoot.definitionList.flatMap { it.findVariablesInSubtree() }
            else -> throw IncorrectPsiTypeException("Value of class ${assignmentRoot.className()} passed as assignment")
        }

    override fun getRightVariables(assignmentRoot: PsiElement): List<PsiElement> =
        when (assignmentRoot) {
            is GoAssignmentStatement -> assignmentRoot.expressionList.flatMap { it.findVariablesInSubtree() }
            is GoShortVarDeclaration -> assignmentRoot.expressionList.flatMap { it.findVariablesInSubtree() }
            is GoVarOrConstSpec<*> -> assignmentRoot.expressionList.flatMap { it.findVariablesInSubtree() }
            else -> throw IncorrectPsiTypeException("Value of class ${assignmentRoot.className()} passed as assignment")
        }
}
