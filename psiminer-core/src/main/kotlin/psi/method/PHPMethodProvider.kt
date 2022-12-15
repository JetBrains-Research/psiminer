package psi.method

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocCommentImpl
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl

class PHPMethodProvider : MethodProvider() {
    override fun getNameNode(root: PsiElement): PsiElement =
        (root as? MethodImpl)?.nameIdentifier
            ?: throw IllegalArgumentException("Try to extract name not from the method")

    override fun getBodyNode(root: PsiElement): PsiElement? =
        (root as? MethodImpl)?.lastChild
            ?: throw IllegalArgumentException("Try to extract name not from the method")

    override fun getDocComment(root: PsiElement): PsiElement? {
        return PsiTreeUtil.collectElementsOfType(root, PhpDocCommentImpl::class.java).firstOrNull()
    }

    override fun getNonDocComments(root: PsiElement): Collection<PsiElement> {
        return PsiTreeUtil.collectElementsOfType(root, PsiComment::class.java).filterNot { it is PhpDocCommentImpl }
    }

    override fun getDocCommentString(root: PsiElement): String {
        val docComment = getDocComment(root)
        return if (docComment == null) {
            ""
        } else {
            stringsToCommentString((docComment as PhpDocCommentImpl).text.split(SPLIT_REGEX))
        }
    }

    override fun getNonDocCommentsString(root: PsiElement): String {
        return stringsToCommentString(getNonDocComments(root).flatMap { it.text.split(SPLIT_REGEX) })
    }

    override fun isConstructor(root: PsiElement): Boolean =
        (root as? MethodImpl)?.name == "__construct"

    override fun hasModifier(root: PsiElement, modifier: String): Boolean =
        (root as? MethodImpl)?.modifier?.toString()?.split(' ')?.contains(modifier)
            ?: throw IllegalArgumentException("Try to extract modifier not from the method")

    companion object {
        val SPLIT_REGEX = Regex("[^A-Za-z\\d]")
    }
}
