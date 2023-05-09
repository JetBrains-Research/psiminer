package psi.method

import astminer.featureextraction.className
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.methods.RMethodBase

class RubyMethodProvider : MethodProvider() {
    override fun getNameNode(root: PsiElement): PsiElement =
        (root as? RMethod)?.methodName?.firstChild?.firstChild
            ?: throw NotAMethodException("name", root.className())

    override fun getBodyNode(root: PsiElement): PsiElement =
        (root as? RMethodBase<*>)?.body
            ?: throw NotAMethodException("body", root.className())

    override fun getDocComment(root: PsiElement): PsiElement? {
        return null
    }

    override fun getNonDocComments(root: PsiElement): Collection<PsiElement> {
        return PsiTreeUtil
            .collectElementsOfType(root, PsiComment::class.java)
    }

    /**
     * TODO: try using annotationData
     */
    override fun getDocCommentString(root: PsiElement): String = ""

    override fun getNonDocCommentsString(root: PsiElement): String {
        return stringsToCommentString(getNonDocComments(root).flatMap { it.text.split(SPLIT_REGEX) })
    }

    override fun isConstructor(root: PsiElement): Boolean =
        (root as? RMethod)?.isConstructor ?: false

    companion object {
        val SPLIT_REGEX = Regex("[^A-Za-z\\d]")
    }
}
