package psi.treeProcessors

import com.intellij.psi.PsiElement

interface PsiTreeProcessor {
    fun process(root: PsiElement)
}
