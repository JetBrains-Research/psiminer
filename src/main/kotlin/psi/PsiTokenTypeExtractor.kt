package psi

import TypeConstants
import com.intellij.psi.*
import com.intellij.psi.util.parentOfType

class PsiTokenTypeExtractor {

    fun extractTokenType(node: PsiElement): String? {
        if (node !is PsiIdentifier) {
            return null
        }
        return when (node.parent) {
            is PsiExpression -> extractFromExpression(node)
            is PsiVariable -> extractFromVariable(node)
            is PsiTypeElement -> extractFromTypeElement(node)
            is PsiMethod -> extractFromMethod(node)
            else -> TypeConstants.UNKNOWN_TYPE
        }
    }

    private fun extractFromExpression(node: PsiIdentifier): String {
        return node.parentOfType<PsiExpression>()?.type?.presentableText ?: TypeConstants.UNKNOWN_TYPE
    }

    private fun extractFromVariable(node: PsiIdentifier): String {
        return node.parentOfType<PsiVariable>()?.type?.presentableText ?: TypeConstants.UNKNOWN_TYPE
    }

    private fun extractFromTypeElement(node: PsiIdentifier): String {
        return node.parentOfType<PsiTypeElement>()?.type?.presentableText ?: TypeConstants.UNKNOWN_TYPE
    }

    private fun extractFromMethod(node: PsiIdentifier): String {
        return node.parentOfType<PsiMethod>()?.returnTypeElement?.type?.presentableText ?: TypeConstants.UNKNOWN_TYPE
    }

}