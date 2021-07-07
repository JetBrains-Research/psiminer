package psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiWhiteSpace
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

fun PsiNamedElement.renameAllSubtreeOccurrences(newName: String) {
    val usages = PsiTreeUtil
        .collectElements(this) { it.textMatches(this.name ?: return@collectElements false) }
        .map { UsageInfo(it) }
        .toTypedArray()
    val renameProcessor = RenamePsiElementProcessor.forElement(this)
    renameProcessor.renameElement(this, newName, usages, null)
}

/**
 * Some modifications during filtering or label extracting may add whitespaces (e.g. after renaming identifiers)
 * Therefore someone need to hide them manually before saving
 */
fun PsiElement.hideWhiteSpaces() =
    PsiTreeUtil.collectElementsOfType(this, PsiWhiteSpace::class.java).forEach { it.isHidden = true }
