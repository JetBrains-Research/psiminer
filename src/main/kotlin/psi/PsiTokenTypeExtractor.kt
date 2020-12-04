package psi

import TreeConstants.NO_TYPE
import com.intellij.psi.*
import com.intellij.psi.util.parentOfType

class PsiTokenTypeExtractor {

    fun extractTokenType(node: PsiElement): String =
        when (node) {
            is PsiLiteralExpression -> node.type?.presentableText ?: NO_TYPE
            is PsiIdentifier -> extractFromIdentifier(node)
            else -> NO_TYPE
        }

    private fun extractFromIdentifier(node: PsiIdentifier): String {
        return when (node.parent) {
            is PsiExpression -> extractFromExpression(node)
            is PsiVariable -> extractFromVariable(node)
            is PsiTypeElement -> extractFromTypeElement(node)
            is PsiMethod -> extractFromMethod(node)
            else -> NO_TYPE
        }
    }

    private fun extractFromExpression(node: PsiIdentifier): String {
        return node.parentOfType<PsiExpression>()?.type?.presentableText ?: NO_TYPE
    }

    private fun extractFromVariable(node: PsiIdentifier): String {
        return node.parentOfType<PsiVariable>()?.type?.presentableText ?: NO_TYPE
    }

    private fun extractFromTypeElement(node: PsiIdentifier): String {
        return node.parentOfType<PsiTypeElement>()?.type?.presentableText ?: NO_TYPE
    }

    private fun extractFromMethod(node: PsiIdentifier): String {
        return node.parentOfType<PsiMethod>()?.returnTypeElement?.type?.presentableText ?: NO_TYPE
    }
}
