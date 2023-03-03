package psi.transformations.removecomments

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import psi.nodeProperties.isHidden
import psi.transformations.PhpTreeTransformation

class PhpRemoveCommentsTransformation(private val removePhpDoc: Boolean) : PhpTreeTransformation {

    override fun transform(root: PsiElement) {
        PsiTreeUtil.collectElementsOfType(root, PsiComment::class.java)
            .filter { it !is PhpDocComment }
            .forEach { it.isHidden = true }
        if (removePhpDoc) {
            PsiTreeUtil.collectElementsOfType(root, PhpDocComment::class.java)
                .forEach { it.isHidden = true }
        }
    }
}
