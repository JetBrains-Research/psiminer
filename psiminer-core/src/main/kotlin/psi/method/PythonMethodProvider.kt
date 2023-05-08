package psi.method

import astminer.featureextraction.className
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyFunction
import com.jetbrains.python.psi.PyStringLiteralExpression

class PythonMethodProvider : MethodProvider() {
    override fun getNameNode(root: PsiElement): PsiElement =
        (root as? PyFunction)?.nameIdentifier
            ?: throw NotAMethodException("name", root.className())

    override fun getBodyNode(root: PsiElement): PsiElement =
        (root as? PyFunction)?.lastChild
            ?: throw NotAMethodException("body", root.className())

    override fun getDocComment(root: PsiElement): PyStringLiteralExpression? {
        return (root as? PyFunction)?.docStringExpression
    }

    override fun getNonDocComments(root: PsiElement): Collection<PsiElement> {
        return PsiTreeUtil
            .collectElementsOfType(root, PsiComment::class.java)
            .filterNot { it is PyStringLiteralExpression }
    }

    override fun getDocCommentString(root: PsiElement): String {
        val docComment = getDocComment(root)
        return if (docComment == null) {
            ""
        } else {
            stringsToCommentString(docComment.stringValue.split(SPLIT_REGEX))
        }
    }

    override fun getNonDocCommentsString(root: PsiElement): String {
        return stringsToCommentString(getNonDocComments(root).flatMap { it.text.split(SPLIT_REGEX) })
    }

    override fun isConstructor(root: PsiElement): Boolean =
        (root as? PyFunction)?.name == "__init__"

    companion object {
        val SPLIT_REGEX = Regex("[^A-Za-z\\d]")
    }
}
