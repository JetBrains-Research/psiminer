package psi.transformations.typeresolve

import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.ElementType
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import psi.transformations.JavaTreeTransformation

class JavaResolveTypeTransformation : JavaTreeTransformation {

    override fun transform(root: PsiElement) {
        val typeResolverVisitor = TypeResolverVisitor()
        root.accept(typeResolverVisitor)
    }

    private inner class TypeResolverVisitor : JavaRecursiveElementVisitor() {
        override fun visitJavaToken(token: PsiJavaToken?) {
            super.visitJavaToken(token)
            if (ElementType.OPERATION_BIT_SET.contains(token?.elementType)) {
                token?.resolvedTokenType = OPERATOR
            }
        }

        override fun visitKeyword(keyword: PsiKeyword?) {
            super.visitKeyword(keyword)
            keyword?.resolvedTokenType = KEYWORD
        }

        override fun visitIdentifier(identifier: PsiIdentifier?) {
            super.visitIdentifier(identifier)
            identifier?.also {
                it.resolvedTokenType = extractFromIdentifier(identifier)
            }
        }

        override fun visitLiteralExpression(expression: PsiLiteralExpression?) {
            super.visitLiteralExpression(expression)
            expression?.children?.forEach {
                it.resolvedTokenType =
                    it.parentOfType<PsiLiteralExpression>()?.type?.presentableText ?: NO_TYPE
            }
        }

        private fun extractFromIdentifier(node: PsiIdentifier): String {
            return when (node.parent) {
                is PsiExpression -> extractFromExpression(node)
                is PsiVariable -> extractFromVariable(node)
                is PsiTypeElement -> extractFromTypeElement(node)
                is PsiMethod -> extractFromMethod(node)
                else -> NO_TYPE
            }
        }

        private fun extractFromExpression(node: PsiIdentifier): String =
            node.parentOfType<PsiExpression>()?.type?.presentableText ?: NO_TYPE

        private fun extractFromVariable(node: PsiIdentifier): String =
            node.parentOfType<PsiVariable>()?.type?.presentableText ?: NO_TYPE

        private fun extractFromTypeElement(node: PsiIdentifier): String =
            node.parentOfType<PsiTypeElement>()?.type?.presentableText ?: NO_TYPE

        private fun extractFromMethod(node: PsiIdentifier): String =
            node.parentOfType<PsiMethod>()?.returnTypeElement?.type?.presentableText ?: NO_TYPE
    }
}
