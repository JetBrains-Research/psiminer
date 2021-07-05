package psi.transformations

import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.ElementType
import com.intellij.psi.util.elementType
import psi.nodeProperties.isHidden
import psi.nodeProperties.nodeType

class JavaCompressOperatorTransformation : JavaTreeTransformation {

    private val compressOperatorVisitor = CompressOperatorVisitor()

    override fun transform(root: PsiElement) = root.accept(compressOperatorVisitor)

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
