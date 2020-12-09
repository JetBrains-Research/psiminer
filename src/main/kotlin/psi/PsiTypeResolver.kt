package psi

import Config
import astminer.common.normalizeToken
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.ElementType
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType

class PsiTypeResolver(private val config: Config) {

    fun resolveType(node: PsiElement): String =
        when {
            ElementType.OPERATION_BIT_SET.contains(node.elementType) -> OPERATOR
            ElementType.KEYWORD_BIT_SET.contains(node.elementType) -> KEYWORD
            node is PsiLiteralExpression -> node.type?.presentableText ?: NO_TYPE
            node is PsiIdentifier -> {
                val resolvedType = extractFromIdentifier(node)
                when {
                    resolvedType == NO_TYPE -> resolvedType
                    config.splitNames -> splitTypeToSubtypes(resolvedType).joinToString("|")
                    else -> normalizeToken(resolvedType, NO_TYPE)
                }
            }
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

    private fun splitTypeToSubtypes(type: String): List<String> = type
        .split("[<>]".toRegex()).flatMap {
            it.trim()
                .split("(?<=[a-z])(?=[A-Z])|_|[0-9]|(?<=[A-Z])(?=[A-Z][a-z])|\\s+".toRegex())
                .map { s -> normalizeToken(s, "") }
                .filter { token -> token.isNotEmpty() }
                .toList()
        }

    companion object {
        private const val KEYWORD = "<KWRD>"
        private const val OPERATOR = "<OP>"
        const val NO_TYPE = "<NT>"
    }
}
