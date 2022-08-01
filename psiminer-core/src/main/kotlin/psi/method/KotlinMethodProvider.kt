package psi.method

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.kdoc.psi.api.KDocElement
import org.jetbrains.kotlin.kdoc.psi.impl.KDocImpl
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtModifierList
import org.jetbrains.kotlin.psi.KtNamedFunction
import kotlin.reflect.full.isSubclassOf

class KotlinMethodProvider : MethodProvider() {

    companion object {
        val SPLIT_REGEX = Regex("[^A-Za-z\\d]")
    }

    override fun getNameNode(root: PsiElement): PsiElement =
        (root as? KtNamedFunction)?.nameIdentifier
            ?: throw IllegalArgumentException("Try to extract body not from the method")

    override fun getBodyNode(root: PsiElement): PsiElement? {
        val methodRoot = root as? KtNamedFunction
            ?: throw IllegalArgumentException("Try to extract body not from the method")
        return methodRoot.bodyBlockExpression
    }

    override fun getDocComment(root: PsiElement): PsiElement? {
        return PsiTreeUtil.collectElementsOfType(root, PsiComment::class.java).firstOrNull { it is KDocElement }
    }

    override fun getNonDocComments(root: PsiElement): Collection<PsiElement> {
        return PsiTreeUtil.collectElementsOfType(root, PsiComment::class.java).filterNot { it is KDocElement }
    }

    override fun getDocCommentString(root: PsiElement): String {
        val docComment = getDocComment(root)
        return if (docComment == null) {
            ""
        } else {
            stringsToCommentString((docComment as KDocImpl).getDefaultSection().getContent().split(SPLIT_REGEX))
        }
    }

    override fun getNonDocCommentsString(root: PsiElement): String {
        return stringsToCommentString(getNonDocComments(root).flatMap { it.text.split(SPLIT_REGEX) })
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
        (root as? KtNamedFunction)
            ?.annotationEntries
            ?.find { it.shortName?.identifier?.equals(annotation) ?: false } != null
}
