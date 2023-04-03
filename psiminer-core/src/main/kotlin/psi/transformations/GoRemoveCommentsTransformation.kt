package psi.transformations

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import psi.nodeProperties.isHidden

class GoRemoveCommentsTransformation() : JavaTreeTransformation {
    override fun transform(root: PsiElement) {
        PsiTreeUtil
            .collectElementsOfType(root, PsiComment::class.java)
            .forEach { it.isHidden = true }
    }
}