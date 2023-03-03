package psi.transformations.removecomments

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.util.PsiTreeUtil
import psi.nodeProperties.isHidden
import psi.transformations.JavaTreeTransformation

class JavaRemoveCommentsTransformation(private val removeJavaDoc: Boolean) : JavaTreeTransformation {
    override fun transform(root: PsiElement) {
        // Hide simple comments
        PsiTreeUtil.collectElementsOfType(root, PsiComment::class.java)
            .filter { it !is PsiDocComment }
            .forEach { it.isHidden = true }
        // Hide JavaDoc if necessary
        if (removeJavaDoc) {
            PsiTreeUtil.collectElementsOfType(root, PsiDocComment::class.java)
                .forEach { it.isHidden = true }
        }
    }
}
