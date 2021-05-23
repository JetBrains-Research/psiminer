package psi

import GranularityLevel
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.openapi.impl.RefactoringFactoryImpl
import psi.nodeProperties.isHidden
import psi.nodeProperties.nodeType
import psi.nodeProperties.token

fun PsiElement.printTree(indent: Int = 0, delimiter: String = "..", indentStep: Int = 2) {
    var newIndent = indent
    if (!isHidden) {
        println("${delimiter.repeat(indent)} $nodeType -- $token")
        newIndent += indentStep
    }
    children.forEach { it.printTree(newIndent, delimiter, indentStep) }
}

fun splitPsiByGranularity(psiTrees: List<PsiElement>, granularity: GranularityLevel): List<PsiElement> =
    psiTrees.flatMap { psiTree ->
        PsiTreeUtil.collectElementsOfType(psiTree, granularity.psiNodeClass)
    }

fun renameAllOccurrences(definition: PsiNamedElement, newName: String) {
    RefactoringFactoryImpl(definition.project).createRename(definition, newName).run()
}
