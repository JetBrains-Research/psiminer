package psi.transformations.excludenode

import com.intellij.psi.*

class ExcludeWhiteSpaceTransformation : ExcludeNodeTransformation() {
    override fun isIgnored(node: PsiElement): Boolean = node is PsiWhiteSpace
}

class ExcludeKeywordTransformation : ExcludeNodeTransformation() {
    override fun isIgnored(node: PsiElement): Boolean = node is PsiKeyword
}

class ExcludeEmptyGrammarListsTransformation : ExcludeNodeTransformation() {
    private val listTypes = listOf(
        PsiReferenceParameterList::class, PsiModifierList::class, PsiReferenceList::class,
        PsiTypeParameterList::class, PsiExpressionList::class, PsiParameterList::class,
        PsiExpressionListStatement::class, PsiAnnotationParameterList::class
    )

    override fun isIgnored(node: PsiElement): Boolean =
        (node.children.isEmpty() || node.text == "()") && listTypes.any { it.isInstance(node) }
}
