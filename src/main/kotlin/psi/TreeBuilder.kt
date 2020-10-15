package psi

import TypeConstants
import astminer.parse.antlr.SimpleNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiParameterList
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.PsiImportList
import com.intellij.psi.PsiPackageStatement
import com.intellij.psi.PsiJavaToken
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReferenceList
import com.intellij.psi.PsiReferenceParameterList
import com.intellij.psi.PsiModifierList
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.util.elementType

class TreeBuilder {

    fun convertPSITree(root: PsiElement): SimpleNode {
        val tree = convertPsiElement(root, null)
        return compressTreeWithTypes(tree)
    }

    private fun convertPsiElement(node: PsiElement, parent: SimpleNode?): SimpleNode {
        val currentNode = SimpleNode(node.elementType.toString(), parent, null)

        // Try to get the node type
        val nodeType = PsiTokenTypeExtractor().extractTokenType(node) ?: TypeConstants.NO_TYPE
        currentNode.setMetadata(TypeConstants.PSI_TYPE_METADATA_KEY, nodeType)

        // Iterate over the children
        val children = node.children.filter {
            validatePsiElement(it) }.map { kid -> convertPsiElement(kid, currentNode)
        }
        currentNode.setChildren(children)

        // Set token if leaf
        if (children.isEmpty() && node !is PsiParameterList) {
            // if parameter list is empty than corresponding node will contain "()"
            currentNode.setToken(node.text)
        }

        return currentNode
    }

    private fun validatePsiElement(node: PsiElement): Boolean {
        val isSkipType =
                node is PsiWhiteSpace || node is PsiDocComment || node is PsiImportList || node is PsiPackageStatement

        val ignoreJavaTokens = listOf("LBRACE", "RBRACE", "LPARENTH", "RPARENTH", "SEMICOLON", "FOR_KEYWORD")
        val isSkipJavaToken = node is PsiJavaToken && node.elementType.toString() in ignoreJavaTokens

        val isConstructor = node is PsiMethod && node.isConstructor

        // Sometimes there are empty lists in leaves, e.g. variable declaration without modifiers
        val isEmptyLeafList = node.children.isEmpty() &&
                (node is PsiReferenceParameterList || node is PsiModifierList || node is PsiReferenceList)

        return !isSkipType && !isConstructor && !isSkipJavaToken && !isEmptyLeafList
    }

    /**
     * Compress paths of intermediate nodes that have a single child into individual nodes.
     * Extend astminer version to keep node token's types
     */
    private fun compressTreeWithTypes(root: SimpleNode): SimpleNode {
        return if (root.getChildren().size == 1) {
            val child = compressTreeWithTypes(root.getChildren().first() as SimpleNode)
            val compressedNode = SimpleNode(
                    root.getTypeLabel() + "|" + child.getTypeLabel(),
                    root.getParent(),
                    child.getToken()
            )
            val childTokenType = child.getMetadata(TypeConstants.PSI_TYPE_METADATA_KEY)
            compressedNode.setMetadata(TypeConstants.PSI_TYPE_METADATA_KEY, childTokenType ?: TypeConstants.NO_TYPE)
            compressedNode.setChildren(child.getChildren())
            compressedNode
        } else {
            root.setChildren(root.getChildren().map { compressTreeWithTypes(it as SimpleNode) }.toMutableList())
            root
        }
    }
}
