package psi.method

import astminer.common.splitToSubtokens
import com.intellij.psi.PsiElement

abstract class MethodProvider {

    abstract fun getNameNode(root: PsiElement): PsiElement
    abstract fun getBodyNode(root: PsiElement): PsiElement?
    abstract fun getDocComment(root: PsiElement): PsiElement?
    abstract fun getNonDocComments(root: PsiElement): Collection<PsiElement>
    abstract fun getDocCommentString(root: PsiElement): String
    abstract fun getNonDocCommentsString(root: PsiElement): String
    abstract fun isConstructor(root: PsiElement): Boolean

    open fun hasModifier(root: PsiElement, modifier: String): Boolean = false
    open fun hasAnnotation(root: PsiElement, annotation: String): Boolean = false

    open fun stringsToCommentString(c: Collection<String>): String {
        return c.filterNot { it.none { s -> s.isLetterOrDigit() } }.flatMap { splitToSubtokens(it) }.joinToString("|")
    }
}
