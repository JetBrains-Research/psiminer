package filter

import Language
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod

class ConstructorFilter : Filter() {
    override fun isGoodTree(root: PsiElement, language: Language) =
        !language.handler.methodProvider.isConstructor(root)
}

/**
 * Filter methods by their modifiers.
 * E.g. [listOf("abstract")] would remove all abstract methods
 * @param ignoreModifiers: list of modifiers to ignore
 */
class ModifierFilter(private val ignoreModifiers: List<String>) : Filter() {
    override fun isGoodTree(root: PsiElement, language: Language): Boolean =
        ignoreModifiers.all { !language.handler.methodProvider.hasModifier(root, it) }
}

/**
 * Filter methods by their annotations.
 * E.g. [listOf("Override")] would remove all overridden methods
 * @param ignoreAnnotations: list of annotations to ignore
 */
class AnnotationFilter(private val ignoreAnnotations: List<String>) : Filter() {
    override fun isGoodTree(root: PsiElement, language: Language): Boolean =
        ignoreAnnotations.all { !language.handler.methodProvider.hasAnnotation(root, it) }
}

class EmptyMethodFilter : Filter() {
    override fun isGoodTree(root: PsiElement, language: Language): Boolean =
        language.handler.methodProvider.getBody(root)?.isNotEmpty() ?: false
}
