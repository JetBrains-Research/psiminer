package psi.transformations.excludenode

import com.intellij.psi.*
import psi.transformations.CommonTreeTransformation

class ExcludeWhiteSpaceTransformation : CommonTreeTransformation, ExcludeNodeTransformation() {
    override fun isIgnored(node: PsiElement): Boolean = node is PsiWhiteSpace
}

class ExcludeKeywordTransformation : CommonTreeTransformation, ExcludeNodeTransformation() {
    override fun isIgnored(node: PsiElement): Boolean = node is PsiKeyword
}

class ExcludeEmptyGrammarListsTransformation : CommonTreeTransformation, ExcludeNodeTransformation() {
    private val listTypes = listOf(
        PsiReferenceParameterList::class, PsiModifierList::class, PsiReferenceList::class,
        PsiTypeParameterList::class, PsiExpressionList::class, PsiParameterList::class,
        PsiExpressionListStatement::class, PsiAnnotationParameterList::class
    )

    override fun isIgnored(node: PsiElement): Boolean =
        (node.children.isEmpty() || node.text == "()") && listTypes.any { it.isInstance(node) }
}
