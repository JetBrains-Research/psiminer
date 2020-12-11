package psi

import Config
import astminer.common.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.ElementType
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.elementType
import psi.PsiNode.Companion.NO_TOKEN

class PsiTreeBuilder(private val config: Config) {
    private val typeResolver = PsiTypeResolver(config)

    fun buildPsiTree(root: PsiElement): PsiNode = compressSingleChildBranches(convertPsiElement(root, null))

    private fun convertPsiElement(node: PsiElement, parent: PsiNode?): PsiNode {
        val resolvedType = typeResolver.resolveType(node)
        val printableType = getPrintableType(node)
        val currentNode = PsiNode(node, parent, resolvedType, printableType)

        // Iterate over the children
        val children = node.children
            .filter { validatePsiElement(it) }
            .map { kid -> convertPsiElement(kid, currentNode) }
        currentNode.setChildren(children)

        // Set token if leaf
        if (children.isEmpty()) {
            currentNode.setNormalizedToken(
                when {
                    numberLiterals.contains(node.elementType) -> NUMBER_LITERAL
                    boolLiterals.contains(node.elementType) -> BOOLEAN_LITERAL
                    ElementType.TEXT_LITERALS.contains(node.elementType) -> STRING_LITERAL
                    ElementType.NULL_KEYWORD == node.elementType -> NULL_LITERAL
                    config.splitNames -> splitToSubtokens(node.text).joinToString("|")
                    else -> normalizeToken(node.text, NO_TOKEN)
                }
            )
        }

        return currentNode
    }

    private fun isSkipType(node: PsiElement): Boolean =
        node is PsiWhiteSpace || node is PsiDocComment || node is PsiImportList || node is PsiPackageStatement ||
                node is PsiTypeElement

    // Skip nodes for commas, semicolons, different brackets, and etc
    private fun isJavaPrintableSymbol(node: PsiElement): Boolean = skipElementTypes.any { node.elementType == it }

    // Sometimes there are empty lists in leaves, e.g. variable declaration without modifiers
    private fun isEmptyList(node: PsiElement): Boolean =
        (node.children.isEmpty() || node.text == "()") && (
                node is PsiReferenceParameterList || node is PsiModifierList || node is PsiReferenceList ||
                        node is PsiTypeParameterList || node is PsiExpressionList || node is PsiParameterList ||
                        node is PsiExpressionListStatement || node is PsiAnnotationParameterList
                )

    private fun isSkipKeyword(node: PsiElement): Boolean = !config.storeKeyword && node is PsiKeyword

    private fun isSkipOperator(node: PsiElement): Boolean =
        config.compressOperators && ElementType.OPERATION_BIT_SET.contains(node.elementType)

    private fun validatePsiElement(node: PsiElement): Boolean =
        !isSkipType(node) && !isJavaPrintableSymbol(node) && !isEmptyList(node) &&
                !isSkipKeyword(node) && !isSkipOperator(node)

    private fun getPrintableType(node: PsiElement): String? {
        if (!config.compressOperators) return null
        return when (node) {
            is PsiBinaryExpression -> "${node.elementType}:${node.operationSign.elementType}"
            is PsiPrefixExpression -> "${node.elementType}:${node.operationSign.elementType}"
            is PsiPostfixExpression -> "${node.elementType}:${node.operationSign.elementType}"
            is PsiAssignmentExpression -> "${node.elementType}:${node.operationSign.elementType}"
            else -> null
        }
    }

    private fun compressSingleChildBranches(node: PsiNode): PsiNode {
        val compressedChildren = node.getChildren().map { compressSingleChildBranches(it) }
        return if (compressedChildren.size == 1) {
            val child = compressedChildren.first()
            val compressedNode = PsiNode(
                node.wrappedNode,
                node.getParent(),
                child.resolvedTokenType,
                "${node.getTypeLabel()}|${child.getTypeLabel()}"
            )
            compressedNode.setNormalizedToken(child.getNormalizedToken())
            compressedNode.setChildren(child.getChildren())
            compressedNode
        } else {
            node.setChildren(compressedChildren)
            node
        }
    }

    companion object {
        private const val NUMBER_LITERAL = "<NUM>"
        private const val STRING_LITERAL = "<STR>"
        private const val BOOLEAN_LITERAL = "<BOOL>"
        private const val NULL_LITERAL = "<NULL>"

        private val boolLiterals = listOf(ElementType.TRUE_KEYWORD, ElementType.FALSE_KEYWORD)
        private val numberLiterals = TokenSet.orSet(ElementType.INTEGER_LITERALS, ElementType.REAL_LITERALS)
        private val skipElementTypes = listOf(
            ElementType.LBRACE,
            ElementType.RBRACE,
            ElementType.LBRACKET,
            ElementType.RBRACKET,
            ElementType.LPARENTH,
            ElementType.RPARENTH,
            ElementType.SEMICOLON,
            ElementType.COMMA,
            ElementType.DOT,
            ElementType.ELLIPSIS,
            ElementType.AT
        )
    }
}
