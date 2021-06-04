package psi.language.method

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtFunction
import kotlin.reflect.full.isSubclassOf

class KotlinMethodProvider : MethodProvider() {

    override fun getName(root: PsiElement): String? {
        val methodRoot = root as? KtFunction ?:
            throw IllegalArgumentException("Try to extract body not from the method")
        return methodRoot.name
    }

    override fun getBody(root: PsiElement): String? {
        val methodRoot = root as? KtFunction ?:
            throw IllegalArgumentException("Try to extract body not from the method")
        return methodRoot.bodyBlockExpression?.toString()
    }

    override fun isConstructor(root: PsiElement): Boolean = root::class.isSubclassOf(KtConstructor::class)

    override fun hasModifier(root: PsiElement, modifier: String): Boolean =
        (root as? KtFunction)?.hasModifier(KtModifierKeywordToken.keywordModifier(modifier)) ?:
        throw IllegalArgumentException("Try to extract modifier not from the method")

    override fun hasAnnotation(root: PsiElement, annotation: String): Boolean =
        (root as? KtFunction)?.annotationEntries?.find { it.shortName?.identifier?.equals(annotation) ?: false } != null
}