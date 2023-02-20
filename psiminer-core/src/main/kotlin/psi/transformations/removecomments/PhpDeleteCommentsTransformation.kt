package psi.transformations.removecomments

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import psi.transformations.PhpTreeModifyingTransformation

class PhpDeleteCommentsTransformation(private val removePhpDoc: Boolean) : PhpTreeModifyingTransformation {

    override fun transform(root: PsiElement) {
        PsiTreeUtil.collectElementsOfType(root, PsiComment::class.java)
            .filter { it !is PhpDocComment }
            .forEach { it.delete() }
        if (removePhpDoc) {
            PsiTreeUtil.collectElementsOfType(root, PhpDocComment::class.java)
                .forEach { it.delete() }
        }
    }
}
