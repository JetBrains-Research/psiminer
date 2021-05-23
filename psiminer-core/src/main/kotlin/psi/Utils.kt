package psi

import GranularityLevel
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.rename.RenamePsiElementProcessor
import com.intellij.usageView.UsageInfo
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

fun renameAllSubtreeOccurrences(root: PsiNamedElement, newName: String) {
    val usages = PsiTreeUtil
        .collectElements(root) { it.textMatches(root.name ?: return@collectElements false) }
        .map { UsageInfo(it) }
        .toTypedArray()
    val renameProcessor = RenamePsiElementProcessor.forElement(root)
    WriteCommandAction.runWriteCommandAction(root.project) {
        renameProcessor.renameElement(root, newName, usages, null)
    }
}
