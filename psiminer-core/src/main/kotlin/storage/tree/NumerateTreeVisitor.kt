package storage.tree

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import psi.nodeProperties.isHidden

class NumerateTreeVisitor : PsiRecursiveElementVisitor() {
    val nodeToId = hashMapOf<PsiElement, Int>()

    override fun visitElement(element: PsiElement) {
        if (!element.isHidden) nodeToId[element] = nodeToId.size
        super.visitElement(element) // super call in the end of recursion method correspond to preorder traverse
    }

    fun orderTree(): List<PsiElement> =
        nodeToId.entries
            .sortedBy { it.value }
            .map { it.key }
}
