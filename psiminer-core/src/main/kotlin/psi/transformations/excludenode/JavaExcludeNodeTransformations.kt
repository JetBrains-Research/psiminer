package psi.transformations.excludenode

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiImportStatement
import com.intellij.psi.PsiPackageStatement
import com.intellij.psi.impl.source.tree.ElementType
import com.intellij.psi.util.elementType
import psi.transformations.JavaTreeTransformation

class ExcludePackageStatementTransformation : JavaTreeTransformation, ExcludeNodeTransformation() {
    override fun isIgnored(node: PsiElement): Boolean = node is PsiPackageStatement
}

class ExcludeImportStatementsTransformation : JavaTreeTransformation, ExcludeNodeTransformation() {
    override fun isIgnored(node: PsiElement): Boolean = node is PsiImportStatement
}

class ExcludeJavaSymbolsTransformation : JavaTreeTransformation, ExcludeNodeTransformation() {
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

    override fun isIgnored(node: PsiElement): Boolean = node.elementType in skipElementTypes
}
