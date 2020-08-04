package psi

import astminer.common.model.Node
import astminer.parse.antlr.SimpleNode
import astminer.parse.antlr.compressTree
import com.intellij.psi.*
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.util.elementType

fun convertPSITree(root: PsiElement): SimpleNode {
    val tree = convertPsiElement(root, null)
    return compressTree(tree)
}

fun convertPsiElement(node: PsiElement, parent: SimpleNode?): SimpleNode {
    val currentNode = SimpleNode(node.elementType.toString(), parent, null)
    val children: MutableList<Node> = ArrayList()

    node.children
        .filter {
            !(it is PsiWhiteSpace || it is PsiDocComment || it is PsiImportStatement || it is PsiPackageStatement)
                    && !(it is PsiMethod && it.isConstructor)
        }
        .forEach {
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
                /* TODO: consider creating ParameterNode at this point
                is PsiParameterList -> {
                    it.parameters.forEach {
                        val returnTypeNodePsi = SimpleNode(it.type.canonicalText, null, it.elementType.toString())
                        val nameNodePsi = it.nameIdentifier

//                        val returnTypeNode = psi.convertPsiElement(returnTypeNodePsi as PsiElement, null)
                        val nameNode = psi.convertPsiElement(nameNodePsi as PsiElement, null)

                        children.add(
                            ParameterNode(it.asSimpleNode(), returnTypeNodePsi, nameNode) as Node
                        )
                    }
                }
                */
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
