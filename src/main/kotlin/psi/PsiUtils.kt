package psi

import astminer.common.model.Node
import astminer.parse.antlr.SimpleNode
import astminer.parse.antlr.compressTree
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiImportStatement
import com.intellij.psi.PsiJavaToken
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiPackageStatement
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.PsiThisExpression
import com.intellij.psi.PsiVariable
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.util.elementType

fun convertPSITree(root: PsiElement): SimpleNode {
    val tree = convertPsiElement(root, null)
    return compressTree(tree)
}

fun validatePsiElement(node: PsiElement): Boolean {
    val isSkipType =
        node is PsiWhiteSpace || node is PsiDocComment || node is PsiImportStatement || node is PsiPackageStatement
    val isConstructor = node is PsiMethod && node.isConstructor
    return !isSkipType && !isConstructor
}

fun convertPsiElement(node: PsiElement, parent: SimpleNode?): SimpleNode {
    val currentNode = SimpleNode(node.elementType.toString(), parent, null)
    val children = mutableListOf<Node>()

    node.children.filter { validatePsiElement(it) }.forEach {
        when (it) {
            is PsiJavaToken -> {
                val n = SimpleNode(it.tokenType.toString(), currentNode, it.text)
                children.add(n)
            }
            is PsiThisExpression -> {
                val token = "this"
                val tokenType = it.type?.presentableText ?: "null"
                val childNode = SimpleNode(it.elementType.toString(), currentNode, token)
                childNode.setMetadata(Config.psiTypeMetadataKey, tokenType)
                children.add(childNode)
            }
            is PsiReferenceExpression -> {
                val token = it.element.text
                val tokenType = it.type?.presentableText ?: "null"
                val childNode = SimpleNode(it.elementType.toString(), currentNode, token)
                childNode.setMetadata(Config.psiTypeMetadataKey, tokenType)
                children.add(childNode)
            }
            is PsiVariable -> {
                val token = it.name
                val tokenType = it.type.presentableText
                val childNode = SimpleNode(it.elementType.toString(), currentNode, token)
                childNode.setMetadata(Config.psiTypeMetadataKey, tokenType)
                children.add(childNode)
                it.children.forEach { kid -> convertPsiElement(kid, childNode) }
            }
//                TODO: consider creating ParameterNode at this point
//                is PsiParameterList -> {
//                    it.parameters.forEach {
//                        val returnTypeNodePsi = SimpleNode(it.type.canonicalText, null, it.elementType.toString())
//                        val nameNodePsi = it.nameIdentifier
//
//  //                    val returnTypeNode = psi.convertPsiElement(returnTypeNodePsi as PsiElement, null)
//                        val nameNode = psi.convertPsiElement(nameNodePsi as PsiElement, null)
//
//                        children.add(
//                            ParameterNode(it.asSimpleNode(), returnTypeNodePsi, nameNode) as Node
//                        )
//                    }
//                }
//                is PsiMethod -> {
//                    if (!it.isConstructor) {
//                        children.add(psi.convertPsiElement(it, currentNode))
//                    }
//                }
            else -> {
                children.add(convertPsiElement(it, currentNode))
            }
        }
    }
    currentNode.setChildren(children)

    return currentNode
}
