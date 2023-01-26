package psi.method

import astminer.featureextraction.className
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl

class PHPMethodProvider : MethodProvider() {
    override fun getNameNode(root: PsiElement): PsiElement =
        (root as? MethodImpl)?.nameIdentifier
            ?: throw NotAMethodException("name", root.className())

    override fun getBodyNode(root: PsiElement): PsiElement =
        (root as? MethodImpl)?.lastChild
            ?: throw NotAMethodException("body", root.className())

    override fun getDocComment(root: PsiElement): PsiElement? {
        return PsiTreeUtil.collectElementsOfType(root, PhpDocComment::class.java).firstOrNull()
    }

    override fun getNonDocComments(root: PsiElement): Collection<PsiElement> {
        return PsiTreeUtil.collectElementsOfType(root, PsiComment::class.java).filterNot { it is PhpDocComment }
    }

    override fun getDocCommentString(root: PsiElement): String {
        val docComment = getDocComment(root)
        return if (docComment == null) {
            ""
        } else {
            stringsToCommentString((docComment as PhpDocComment).text.split(SPLIT_REGEX))
        }
    }

    override fun getNonDocCommentsString(root: PsiElement): String {
        return stringsToCommentString(getNonDocComments(root).flatMap { it.text.split(SPLIT_REGEX) })
    }

    override fun isConstructor(root: PsiElement): Boolean =
        (root as? MethodImpl)?.name == "__construct"

    /**
     * Strange but casting to PsiMethod doesn't work here.
     * TODO: investigate why
     */
    override fun hasModifier(root: PsiElement, modifier: String): Boolean =
        (root as? MethodImpl)?.modifier?.toString()?.split(' ')?.contains(modifier)
            ?: throw NotAMethodException("modifier", root.className())

    companion object {
        val SPLIT_REGEX = Regex("[^A-Za-z\\d]")
    }
}
