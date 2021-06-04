package psi.language.method

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod

class JavaMethodProvider : MethodProvider() {

    override fun getName(root: PsiElement): String =
        (root as? PsiMethod)?.name ?:
        throw IllegalArgumentException("Try to extract name not from the method")

    override fun getBody(root: PsiElement): String? {
        val methodRoot = root as? PsiMethod ?: throw IllegalArgumentException("Try to extract body not from the method")
        return methodRoot.body?.toString()
    }

    override fun isConstructor(root: PsiElement): Boolean =
        (root as? PsiMethod)?.isConstructor ?: false

    override fun hasModifier(root: PsiElement, modifier: String): Boolean =
        (root as? PsiMethod)?.hasModifierProperty(modifier) ?:
        throw IllegalArgumentException("Try to extract modifier not from the method")

    override fun hasAnnotation(root: PsiElement, annotation: String): Boolean =
        (root as? PsiMethod)?.hasAnnotation(annotation) ?:
        throw IllegalArgumentException("Try to extract annotation not from the method")
}