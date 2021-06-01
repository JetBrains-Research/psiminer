package psi.treeProcessors

import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.ElementType
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import psi.nodeProperties.isHidden
import psi.nodeProperties.nodeType
import psi.nodeProperties.technicalToken

interface JavaTreeProcessor : PsiTreeProcessor

class HideLiterals(
    private val hideNumbers: Boolean = false,
    private val numberWhiteList: List<Int> = listOf(0, 1, 32, 64),
    private val hideStrings: Boolean = false
) : JavaTreeProcessor {
    override fun process(root: PsiElement) =
        PsiTreeUtil.collectElementsOfType(root, PsiJavaToken::class.java)
            .forEach { token ->
                if (hideNumbers &&
                    numberLiterals.contains(token.elementType) &&
                    numberWhiteList.all { !token.textMatches(it.toString()) }) {
                    token.technicalToken = NUMBER_LITERAL
                }
                if (hideStrings && stringLiterals.contains(token.elementType)) {
                    token.technicalToken = STRING_LITERAL
                }
            }

    companion object {
        const val NUMBER_LITERAL = "<NUM>"
        val numberLiterals = TokenSet.orSet(ElementType.INTEGER_LITERALS, ElementType.REAL_LITERALS)

        const val STRING_LITERAL = "<STR>"
        val stringLiterals = TokenSet.orSet(ElementType.STRING_LITERALS, ElementType.TEXT_LITERALS)
    }
}

class CompressOperators : JavaTreeProcessor {
    private val compressOperatorVisitor = CompressOperatorVisitor()

    override fun process(root: PsiElement) = root.accept(compressOperatorVisitor)

    private inner class CompressOperatorVisitor : JavaRecursiveElementVisitor() {
        override fun visitExpression(expression: PsiExpression?) {
            super.visitExpression(expression)
            when (expression) {
                is PsiBinaryExpression -> expression.nodeType += ":" + expression.operationSign.elementType
                is PsiAssignmentExpression -> expression.nodeType += ":" + expression.operationSign.elementType
                is PsiPrefixExpression -> expression.nodeType += ":" + expression.operationSign.elementType
                is PsiPostfixExpression -> expression.nodeType += ":" + expression.operationSign.elementType
            }
        }

        override fun visitJavaToken(token: PsiJavaToken?) {
            super.visitJavaToken(token)
            if (token?.elementType in ElementType.OPERATION_BIT_SET) token?.isHidden = true
        }
    }
}

class RemoveComments(private val removeJavaDoc: Boolean) : JavaTreeProcessor {
    override fun process(root: PsiElement) {
        val comments = PsiTreeUtil.collectElementsOfType(root, PsiComment::class.java)
            .filter { if (!removeJavaDoc) it !is PsiDocComment else true }
        // We should handle each group of elements with same parent separately
        // since if we delete the first item in this group we invalidate
        // other elements with the same parent
        comments.toList().groupBy { it.parent }.entries.forEach {
            // If we do not change the order of the elements, then a parent can invalidate
            // the child element, but it can also be a comment and an exception will be thrown,
            // so we must delete the found comments in the reverse order
            it.value.reversed().forEach { it.delete() }
        }
    }
}
