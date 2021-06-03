package psi.transformation

import com.intellij.psi.PsiElement

interface PsiTreeTransformer {
    fun transform(root: PsiElement)
}
