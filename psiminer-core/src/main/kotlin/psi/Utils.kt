package psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import psi.nodeProperties.isHidden
import psi.nodeProperties.nodeType
import psi.nodeProperties.token

fun PsiElement.printTree(delimiter: String = "..", indentStep: Int = 2) {
    val visitor = PsiPrintVisitor(delimiter, indentStep)
    accept(visitor)
}

class PsiPrintVisitor(
    private val delimiter: String = "..",
    private val indentStep: Int = 2
) : PsiRecursiveElementVisitor() {
    private val depths = mutableMapOf<PsiElement, Int>()

    override fun visitElement(element: PsiElement) {
        val indent = depths[element.parent]?.plus(1) ?: 0
        depths[element] = indent
        println("${delimiter.repeat(indent * indentStep)} ${element.nodeType} -- ${element.token}")
        super.visitElement(element)
    }
}

/**
 * Some modifications during filtering or label extracting may add whitespaces (e.g. after renaming identifiers)
 * Therefore someone need to hide them manually before saving
 */
fun PsiElement.hideWhiteSpaces() =
    PsiTreeUtil.collectElementsOfType(this, PsiWhiteSpace::class.java).forEach { it.isHidden = true }

/**
 * Calculate size of PSI tree
 */
fun PsiElement.treeSize() =
    PsiTreeUtil.collectElementsOfType(this, PsiElement::class.java).size
