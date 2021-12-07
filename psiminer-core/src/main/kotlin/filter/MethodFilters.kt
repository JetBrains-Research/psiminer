package filter

import com.intellij.psi.PsiElement
import getCleanCode
import psi.language.LanguageHandler

class ConstructorFilter : Filter {
    override fun validateTree(root: PsiElement, languageHandler: LanguageHandler) =
        !languageHandler.methodProvider.isConstructor(root)
}

/**
 * Filter methods by their modifiers.
 * E.g. [listOf("abstract")] would remove all abstract methods
 * @param ignoreModifiers: list of modifiers to ignore
 */
class ModifierFilter(private val ignoreModifiers: List<String>) : Filter {
    override fun validateTree(root: PsiElement, languageHandler: LanguageHandler): Boolean =
        ignoreModifiers.all { !languageHandler.methodProvider.hasModifier(root, it) }
}

/**
 * Filter methods by their annotations.
 * E.g. [listOf("Override")] would remove all overridden methods
 * @param ignoreAnnotations: list of annotations to ignore
 */
class AnnotationFilter(private val ignoreAnnotations: List<String>) : Filter {
    override fun validateTree(root: PsiElement, languageHandler: LanguageHandler): Boolean =
        ignoreAnnotations.all { !languageHandler.methodProvider.hasAnnotation(root, it) }
}

class EmptyMethodFilter : Filter {
    override fun validateTree(root: PsiElement, languageHandler: LanguageHandler): Boolean {
        val body = languageHandler.methodProvider.getBodyNode(root) ?: return false
        val cleanCode = getCleanCode(body.text)
        return cleanCode.isNotEmpty()
    }
}
