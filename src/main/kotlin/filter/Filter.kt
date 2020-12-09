package filter

import com.intellij.psi.PsiMethod
import psi.PsiNode

interface Filter {
    fun checkTree(root: PsiNode): Boolean
}

class ClassConstructorFilter : Filter {
    override fun checkTree(root: PsiNode) = root.wrappedNode is PsiMethod && root.wrappedNode.isConstructor
    companion object {
        const val name = "constructor"
    }
}