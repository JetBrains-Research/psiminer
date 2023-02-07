package psi.transformations.removecomments

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import psi.nodeProperties.isHidden
import psi.transformations.PhpTreeTransformation

class PhpRemoveCommentsTransformation(private val removeDoc: Boolean) : PhpTreeTransformation {

    override fun transform(root: PsiElement) {
        // Hide simple comments
        PsiTreeUtil.collectElementsOfType(root, PsiComment::class.java)
            .filter { it !is PhpDocComment }
            .forEach { it.isHidden = true }
        // Hide PhpDoc if necessary
        if (removeDoc) {
            PsiTreeUtil.collectElementsOfType(root, PhpDocComment::class.java)
                .forEach { it.isHidden = true }
        }
    }

}