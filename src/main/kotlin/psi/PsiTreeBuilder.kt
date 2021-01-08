package psi

import Config
import astminer.common.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.ElementType
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.elementType
import psi.PsiNode.Companion.EMPTY_TOKEN

class PsiTreeBuilder(private val config: Config) {
    private val typeResolver = PsiTypeResolver(config)

    fun buildPsiTree(root: PsiElement): PsiNode {
        val tree = convertPsiElement(root, null)
        return if (config.compressTree) compressSingleChildBranches(tree)
        else tree
    }

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
                if (numberLiterals.contains(node.elementType)) {
                    if (numberWhiteList.contains(node.text)) node.text else NUMBER_LITERAL
                } else {
                    val normalizedToken = normalizeToken(node.text, EMPTY_TOKEN)
                    val splitToken = splitToSubtokens(node.text).joinToString("|")
                    if (config.splitNames && splitToken.isNotEmpty()) splitToken else normalizedToken
                }
            )
        }

        return currentNode
    }

    private fun isSkipType(node: PsiElement): Boolean =
        node is PsiWhiteSpace || node is PsiImportList || node is PsiPackageStatement

    // Skip nodes for commas, semicolons, different brackets, and etc
    private fun isJavaPrintableSymbol(node: PsiElement): Boolean = skipElementTypes.any { node.elementType == it }

    // Sometimes there are empty lists in leaves, e.g. variable declaration without modifiers
    private fun isEmptyList(node: PsiElement): Boolean =
        (node.children.isEmpty() || node.text == "()") && (
                node is PsiReferenceParameterList || node is PsiModifierList || node is PsiReferenceList ||
                        node is PsiTypeParameterList || node is PsiExpressionList || node is PsiParameterList ||
                        node is PsiExpressionListStatement || node is PsiAnnotationParameterList
                )

    private fun isSkipKeyword(node: PsiElement): Boolean = config.removeKeyword && node is PsiKeyword

    private fun isSkipOperator(node: PsiElement): Boolean =
        config.compressOperators && ElementType.OPERATION_BIT_SET.contains(node.elementType)

    private fun isSkipComment(node: PsiElement): Boolean = config.removeComments && node is PsiComment

    private fun validatePsiElement(node: PsiElement): Boolean =
        !isSkipType(node) && !isJavaPrintableSymbol(node) && !isEmptyList(node) &&
                !isSkipKeyword(node) && !isSkipOperator(node) && !isSkipComment(node)

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
                child.wrappedNode,
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

        private val numberLiterals = TokenSet.orSet(ElementType.INTEGER_LITERALS, ElementType.REAL_LITERALS)
        private val numberWhiteList = listOf("0", "1", "32", "64")
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
