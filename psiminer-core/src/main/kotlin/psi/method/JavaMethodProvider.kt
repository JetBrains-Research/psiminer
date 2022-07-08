package psi.method

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.util.PsiTreeUtil

class JavaMethodProvider : MethodProvider() {

    override fun getNameNode(root: PsiElement): PsiElement =
        (root as? PsiMethod)?.nameIdentifier
            ?: throw IllegalArgumentException("Try to extract name not from the method")

    override fun getBodyNode(root: PsiElement): PsiElement? {
        val methodRoot = root as? PsiMethod
            ?: throw IllegalArgumentException("Try to extract body not from the method")
        return methodRoot.body
    }

    override fun getDocComment(root: PsiElement): PsiElement? {
        return PsiTreeUtil.collectElementsOfType(root, PsiDocComment::class.java).firstOrNull()
    }

    override fun getNonDocComments(root: PsiElement): Collection<PsiElement> {
        return PsiTreeUtil.collectElementsOfType(root, PsiComment::class.java).filterNot { it is PsiDocComment }
    }

    override fun isConstructor(root: PsiElement): Boolean =
        (root as? PsiMethod)?.isConstructor ?: false

    override fun hasModifier(root: PsiElement, modifier: String): Boolean =
        (root as? PsiMethod)?.hasModifierProperty(modifier)
            ?: throw IllegalArgumentException("Try to extract modifier not from the method")

    override fun hasAnnotation(root: PsiElement, annotation: String): Boolean =
        (root as? PsiMethod)?.hasAnnotation(annotation)
            ?: throw IllegalArgumentException("Try to extract annotation not from the method")
}
