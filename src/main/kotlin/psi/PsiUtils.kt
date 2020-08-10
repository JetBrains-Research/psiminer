package psi

import Config
import astminer.parse.antlr.SimpleNode
import astminer.parse.antlr.compressTree
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiExpressionList
import com.intellij.psi.PsiImportList
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiPackageStatement
import com.intellij.psi.PsiParameterList
import com.intellij.psi.PsiReferenceList
import com.intellij.psi.PsiReferenceParameterList
import com.intellij.psi.PsiTypeElement
import com.intellij.psi.PsiTypeParameter
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
        node is PsiWhiteSpace || node is PsiDocComment || node is PsiImportList || node is PsiPackageStatement
    val isConstructor = node is PsiMethod && node.isConstructor
    return !isSkipType && !isConstructor
}

fun convertPsiElement(node: PsiElement, parent: SimpleNode?): SimpleNode {
    val currentNode = SimpleNode(node.elementType.toString(), parent, node.text)

    // Try to get the node type
    currentNode.setMetadata(Config.psiTypeMetadataKey, when (node) {
        is PsiExpression -> {
            node.type?.presentableText ?: "UnknownExpression"
        }
        is PsiVariable -> {
            node.type.presentableText
        }
        is PsiTypeElement -> {
            node.type.presentableText
        }
        else -> {
//            println(node.javaClass.canonicalName)
            Config.unknownType
        }
    })

    // Iterate over the children
    val children = when (node) {
        is PsiReferenceList -> {
            node.referenceElements
        }
        is PsiReferenceParameterList -> {
            node.typeParameterElements
        }
        is PsiParameterList -> {
            node.parameters
        }
        is PsiExpressionList -> {
            node.expressions
        }
        is PsiExpression, is PsiVariable, is PsiTypeParameter -> {
            arrayOf()
        }
        else -> {
            node.children
        }
    }.filter { validatePsiElement(it) }.map { kid -> convertPsiElement(kid, currentNode) }
    currentNode.setChildren(children)

    return currentNode
}

fun printPsi(node: PsiElement, indent: Int = 0, character: String = " ", indentStep: Int = 4) {
    println("${character.repeat(indent)}${node.elementType}")
    node.children.forEach {
        printPsi(it, indent + indentStep, character, indentStep)
    }
}
