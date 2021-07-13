package psi.method

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtModifierList
import kotlin.reflect.full.isSubclassOf

class KotlinMethodProvider : MethodProvider() {

    override fun getName(root: PsiElement): String? {
        val methodRoot = root as? KtFunction
            ?: throw IllegalArgumentException("Try to extract body not from the method")
        return methodRoot.name
    }

    override fun getBody(root: PsiElement): String? {
        val methodRoot = root as? KtFunction
            ?: throw IllegalArgumentException("Try to extract body not from the method")
        val block = methodRoot.bodyBlockExpression ?: return null
        return (block as PsiElement).text
    }

    override fun isConstructor(root: PsiElement): Boolean = root::class.isSubclassOf(KtConstructor::class)

    override fun hasModifier(root: PsiElement, modifier: String): Boolean {
        val modifiersList = root.children.firstOrNull { it is KtModifierList } ?: return false
        val modifiers = PsiTreeUtil
            .collectElementsOfType(modifiersList, PsiElement::class.java)
            .filter { it !is PsiWhiteSpace && it !is KtModifierList }
            .map { it.text }
        return modifier in modifiers
    }

    override fun hasAnnotation(root: PsiElement, annotation: String): Boolean =
        (root as? KtFunction)?.annotationEntries?.find { it.shortName?.identifier?.equals(annotation) ?: false } != null
}
