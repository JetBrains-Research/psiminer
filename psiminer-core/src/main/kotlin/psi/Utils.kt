package psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import psi.nodeProperties.isHidden
import psi.nodeProperties.nodeType
import psi.nodeProperties.token

/**
 * Calculate size of PSI tree
 */
fun PsiElement.treeSize() =
    PsiTreeUtil.collectElementsOfType(this, PsiElement::class.java).size

class PreOrderPsiTreeVisitor(private val processor: (PsiElement) -> Unit) : PsiRecursiveElementVisitor() {
    override fun visitElement(element: PsiElement) {
        if (element.isHidden) return
        processor(element)
        super.visitElement(element)
    }
}

fun PsiElement.preOrder(processor: (PsiElement) -> Unit) {
    val visitor = PreOrderPsiTreeVisitor(processor)
    accept(visitor)
}

fun PsiElement.printTree(delimiter: String = "..", indentStep: Int = 2) {
    val depths = mutableMapOf<PsiElement, Int>()
    preOrder {
        val indent = depths[it.parent]?.plus(1) ?: 0
        depths[it] = indent
        println("${delimiter.repeat(indent * indentStep)} ${it.nodeType} -- ${it.token}")
    }
}
