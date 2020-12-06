package psi

import TreeConstants
import astminer.common.getNormalizedToken
import astminer.common.setNormalizedToken
import astminer.common.splitToSubtokens
import astminer.parse.antlr.SimpleNode
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.ElementType
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.util.elementType

class TreeBuilder {

    private val typeExtractor = PsiTokenTypeExtractor()

    fun convertPSITree(root: PsiElement): SimpleNode {
        val tree = convertPsiElement(root, null)
        return compressTreeWithTypes(tree)
    }

    private fun convertPsiElement(node: PsiElement, parent: SimpleNode?): SimpleNode {
        val currentNode = SimpleNode(node.elementType.toString(), parent, null)
        currentNode.setMetadata(TreeConstants.resolvedType, typeExtractor.extractTokenType(node))

        // Iterate over the children
        val children = node.children.filter {
            validatePsiElement(it)
        }.map { kid ->
            convertPsiElement(kid, currentNode)
        }
        currentNode.setChildren(children)

        // Set token if leaf
        if (children.isEmpty()) {
            currentNode.setToken(node.text)
            currentNode.setNormalizedToken(
                if (node is PsiLiteralExpression) {
                    when (node.type?.presentableText) {
                        "int" -> TreeConstants.numberLiteralToken
                        "string" -> TreeConstants.stringLiteralToken
                        "boolean" -> TreeConstants.booleanLiteralToken
                        else -> TreeConstants.defaultLiteralToken
                    }
                } else splitToSubtokens(node.text).joinToString("|")
            )
        }

        return currentNode
    }

    private fun isSkipType(node: PsiElement): Boolean =
        node is PsiWhiteSpace || node is PsiDocComment || node is PsiImportList ||
                node is PsiPackageStatement

    // Skip nodes for commas, semicolons, different brackets, and etc
    private fun isJavaSymbol(node: PsiElement): Boolean {
        val skipElementTypes = listOf(
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
        return skipElementTypes.any { node.elementType == it }
    }

    private fun isConstructor(node: PsiElement): Boolean =
        node is PsiMethod && node.isConstructor

    // Sometimes there are empty lists in leaves, e.g. variable declaration without modifiers
    private fun isEmptyList(node: PsiElement): Boolean =
        (node.children.isEmpty() || node.text == "()") && (
                node is PsiReferenceParameterList || node is PsiModifierList || node is PsiReferenceList ||
                        node is PsiTypeParameterList || node is PsiExpressionList || node is PsiParameterList ||
                        node is PsiExpressionListStatement
                )

    private fun validatePsiElement(node: PsiElement): Boolean =
        !isSkipType(node) && !isJavaSymbol(node) && !isConstructor(node) && !isEmptyList(node)

    /**
     * Compress paths of intermediate nodes that have a single child into individual nodes.
     * Extend AST-miner version to keep node token's types
     */
    private fun compressTreeWithTypes(root: SimpleNode): SimpleNode {
        return if (root.getChildren().size == 1) {
            val child = compressTreeWithTypes(root.getChildren().first() as SimpleNode)
            val compressedNode = SimpleNode(
                root.getTypeLabel() + "|" + child.getTypeLabel(),
                root.getParent(),
                child.getToken()
            )
            compressedNode.setNormalizedToken(child.getNormalizedToken())
            val childTokenType = child.getMetadata(TreeConstants.resolvedType)
            compressedNode.setMetadata(TreeConstants.resolvedType, childTokenType ?: TreeConstants.noType)
            compressedNode.setChildren(child.getChildren())
            compressedNode
        } else {
            root.setChildren(root.getChildren().map { compressTreeWithTypes(it as SimpleNode) }.toMutableList())
            root
        }
    }
}
