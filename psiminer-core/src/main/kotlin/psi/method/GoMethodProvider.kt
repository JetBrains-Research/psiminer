package psi.method

import astminer.featureextraction.className
import com.goide.psi.GoFunctionOrMethodDeclaration
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

class GoMethodProvider : MethodProvider() {
    override fun getNameNode(root: PsiElement): PsiElement =
        (root as? GoFunctionOrMethodDeclaration)?.nameIdentifier
            ?: throw NotAMethodException("name", root.className())

    override fun getBodyNode(root: PsiElement): PsiElement? {
        val methodRoot = root as? GoFunctionOrMethodDeclaration
            ?: throw NotAMethodException("body", root.className())
        return methodRoot.block
    }

    /**
     * Go has DocComments, however there is no special PSI element for them,
     * so it's hard to distinguish them from ordinary comments.
     * Nevertheless, it's still possible because some tools (mainly written on GO)
     * enable this.
     * See more: https://tip.golang.org/doc/comment
     */
    override fun getDocComment(root: PsiElement): PsiElement? {
        return null
    }

    override fun getNonDocComments(root: PsiElement): Collection<PsiElement> {
        return PsiTreeUtil.collectElementsOfType(root, PsiComment::class.java)
    }

    override fun getDocCommentString(root: PsiElement): String {
        return ""
    }

    override fun getNonDocCommentsString(root: PsiElement): String {
        return stringsToCommentString(getNonDocComments(root).flatMap { it.text.split(SPLIT_REGEX) })
    }

    /**
     * Life without classes is such a simple thing.
     */
    override fun isConstructor(root: PsiElement): Boolean {
        return false
    }

    companion object {
        val SPLIT_REGEX = Regex("[^A-Za-z\\d]")
    }
}
