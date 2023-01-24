package psi.method

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.util.PsiTreeUtil

class JavaMethodProvider : MethodProvider() {

    companion object {
        val SPLIT_REGEX = Regex("[^A-Za-z\\d]")
    }

    override fun getNameNode(root: PsiElement): PsiElement =
        (root as? PsiMethod)?.nameIdentifier
            ?: throw NotAMethodException("name")

    override fun getBodyNode(root: PsiElement): PsiElement? {
        val methodRoot = root as? PsiMethod
            ?: throw NotAMethodException("body")
        return methodRoot.body
    }

    override fun getDocComment(root: PsiElement): PsiElement? {
        return PsiTreeUtil.collectElementsOfType(root, PsiDocComment::class.java).firstOrNull()
    }

    override fun getNonDocComments(root: PsiElement): Collection<PsiElement> {
        return PsiTreeUtil.collectElementsOfType(root, PsiComment::class.java).filterNot { it is PsiDocComment }
    }

    override fun getDocCommentString(root: PsiElement): String {
        val docComment = getDocComment(root)
        return if (docComment == null) {
            ""
        } else {
            stringsToCommentString((docComment as PsiDocComment).descriptionElements.map { it.text }
                .flatMap { it.split(SPLIT_REGEX) })
        }
    }

    override fun getNonDocCommentsString(root: PsiElement): String {
        return stringsToCommentString(getNonDocComments(root).flatMap { it.text.split(SPLIT_REGEX) })
    }

    override fun isConstructor(root: PsiElement): Boolean =
        (root as? PsiMethod)?.isConstructor ?: false

    override fun hasModifier(root: PsiElement, modifier: String): Boolean =
        (root as? PsiMethod)?.hasModifierProperty(modifier)
            ?: throw NotAMethodException("modifier")

    override fun hasAnnotation(root: PsiElement, annotation: String): Boolean =
        (root as? PsiMethod)?.hasAnnotation(annotation)
            ?: throw NotAMethodException("annotation")
}
