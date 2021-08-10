package psi.transformations.excludenode

import com.intellij.psi.*
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

class ExcludeKeywordTransformation : JavaTreeTransformation, ExcludeNodeTransformation() {
    override fun isIgnored(node: PsiElement): Boolean = node is PsiKeyword
}

class ExcludeEmptyGrammarListsTransformation : JavaTreeTransformation, ExcludeNodeTransformation() {
    private val listTypes = listOf(
        PsiReferenceParameterList::class, PsiModifierList::class, PsiReferenceList::class,
        PsiTypeParameterList::class, PsiExpressionList::class, PsiParameterList::class,
        PsiExpressionListStatement::class, PsiAnnotationParameterList::class
    )

    override fun isIgnored(node: PsiElement): Boolean =
        (node.children.isEmpty() || node.text == "()") && listTypes.any { it.isInstance(node) }
}
