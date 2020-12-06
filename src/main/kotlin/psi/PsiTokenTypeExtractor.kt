package psi

import Config
import TreeConstants.keywordType
import TreeConstants.noType
import TreeConstants.operatorType
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.ElementType
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import splitTypeToSubtypes

class PsiTokenTypeExtractor {

    fun extractTokenType(node: PsiElement): String {
        if (ElementType.OPERATION_BIT_SET.contains(node.elementType)) return operatorType
        return when (node) {
            is PsiLiteralExpression -> node.type?.presentableText ?: noType
            is PsiKeyword -> keywordType
            is PsiIdentifier -> {
                val resolvedType = extractFromIdentifier(node)
                if (resolvedType == noType || !Config.splitTypes) return resolvedType
                else splitTypeToSubtypes(resolvedType).joinToString("|")
            }
            else -> noType
        }
    }

    private fun extractFromIdentifier(node: PsiIdentifier): String {
        return when (node.parent) {
            is PsiExpression -> extractFromExpression(node)
            is PsiVariable -> extractFromVariable(node)
            is PsiTypeElement -> extractFromTypeElement(node)
            is PsiMethod -> extractFromMethod(node)
            else -> noType
        }
    }

    private fun extractFromExpression(node: PsiIdentifier): String {
        return node.parentOfType<PsiExpression>()?.type?.presentableText ?: noType
    }

    private fun extractFromVariable(node: PsiIdentifier): String {
        return node.parentOfType<PsiVariable>()?.type?.presentableText ?: noType
    }

    private fun extractFromTypeElement(node: PsiIdentifier): String {
        return node.parentOfType<PsiTypeElement>()?.type?.presentableText ?: noType
    }

    private fun extractFromMethod(node: PsiIdentifier): String {
        return node.parentOfType<PsiMethod>()?.returnTypeElement?.type?.presentableText ?: noType
    }
}
