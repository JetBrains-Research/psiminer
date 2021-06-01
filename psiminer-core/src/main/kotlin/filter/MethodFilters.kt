package filter

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod

class ConstructorFilter : Filter() {
    override fun isGoodTree(root: PsiElement) = root is PsiMethod && !root.isConstructor
}

/**
 * Filter methods by their modifiers.
 * E.g. [listOf("abstract")] would remove all abstract methods
 * @param ignoreModifiers: list of modifiers to ignore
 */
class ModifierFilter(private val ignoreModifiers: List<String>) : Filter() {
    override fun isGoodTree(root: PsiElement): Boolean =
        root is PsiMethod && ignoreModifiers.all { !root.modifierList.hasModifierProperty(it) }
}

/**
 * Filter methods by their annotations.
 * E.g. [listOf("Override")] would remove all overridden methods
 * @param ignoreAnnotations: list of annotations to ignore
 */
class AnnotationFilter(private val ignoreAnnotations: List<String>) : Filter() {
    override fun isGoodTree(root: PsiElement): Boolean =
        root is PsiMethod && ignoreAnnotations.all { !root.hasAnnotation(it) }
}

class EmptyMethodFilter : Filter() {
    override fun isGoodTree(root: PsiElement): Boolean = root is PsiMethod && !(root.body?.isEmpty ?: true)
}
