package psi.transformations

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.util.PsiTreeUtil
import psi.nodeProperties.isHidden

class JavaRemoveCommentsTransformation(private val removeJavaDoc: Boolean) : JavaTreeTransformation {
    override fun transform(root: PsiElement) =
        PsiTreeUtil
            .collectElementsOfType(root, PsiComment::class.java)
            .filter { if (!removeJavaDoc) it !is PsiDocComment else true }
            .forEach { it.isHidden = true }
}
