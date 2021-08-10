package psi.transformations

import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaToken
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.impl.source.tree.ElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import psi.nodeProperties.technicalToken

class JavaHideLiteralsTransformation(
    private val hideNumbers: Boolean = false,
    private val numberWhiteList: List<Int> = listOf(0, 1, 32, 64),
    private val hideStrings: Boolean = false
) : JavaTreeTransformation {
    override fun transform(root: PsiElement) {
        val javaHideLiteralsTransformation = JavaHideLiteralsVisitor()
        root.accept(javaHideLiteralsTransformation)
    }

    private inner class JavaHideLiteralsVisitor: JavaRecursiveElementVisitor() {
        override fun visitLiteralExpression(expression: PsiLiteralExpression?) {
            super.visitLiteralExpression(expression)
            expression?.children?.forEach {
                // Hide number literal if needed
                if (hideNumbers && numberLiterals.contains(it.elementType)) {
                    if (numberWhiteList.any { num -> it.textMatches(num.toString()) }) it.technicalToken = it.text
                    else it.technicalToken = NUMBER_LITERAL
                }
                // Hide string literal if needed
                if (hideStrings && stringLiterals.contains(it.elementType)) it.technicalToken = STRING_LITERAL
            }
        }
    }

    companion object {
        const val NUMBER_LITERAL = "<NUM>"
        val numberLiterals = TokenSet.orSet(ElementType.INTEGER_LITERALS, ElementType.REAL_LITERALS)

        const val STRING_LITERAL = "<STR>"
        val stringLiterals = TokenSet.orSet(ElementType.STRING_LITERALS, ElementType.TEXT_LITERALS)
    }
}
