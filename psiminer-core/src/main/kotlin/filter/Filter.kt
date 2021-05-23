package filter

import com.intellij.psi.PsiElement

interface Filter {
    fun isGoodTree(root: PsiElement): Boolean
}
