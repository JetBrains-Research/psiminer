package psi.method

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

abstract class MethodProvider {

    companion object {
        val SPLIT_REGEX = Regex("[^A-Za-z\\d]")
    }

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
        return c.filterNot { it.none { s -> s.isLetterOrDigit() } }.joinToString("|") { it.toLowerCaseAsciiOnly() }
    }
}
