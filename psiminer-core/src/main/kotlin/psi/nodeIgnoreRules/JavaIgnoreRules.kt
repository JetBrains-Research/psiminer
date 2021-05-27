package psi.nodeIgnoreRules

import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.ElementType
import com.intellij.psi.util.elementType

interface JavaIgnoreRule : PsiNodeIgnoreRule

class PackageStatementIgnoreRule : JavaIgnoreRule {
    override fun isIgnored(node: PsiElement): Boolean = node is PsiPackageStatement
}

class ImportStatementIgnoreRule : JavaIgnoreRule {
    override fun isIgnored(node: PsiElement): Boolean = node is PsiImportStatement
}

class JavaSymbolsIgnoreRule : JavaIgnoreRule {
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
