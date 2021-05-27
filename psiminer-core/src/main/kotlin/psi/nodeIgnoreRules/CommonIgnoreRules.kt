package psi.nodeIgnoreRules

import com.intellij.psi.*

interface CommonIgnoreRule : PsiNodeIgnoreRule

class WhiteSpaceIgnoreRule : CommonIgnoreRule {
    override fun isIgnored(node: PsiElement): Boolean = node is PsiWhiteSpace
}

class KeywordIgnoreRule : CommonIgnoreRule {
    override fun isIgnored(node: PsiElement): Boolean = node is PsiKeyword
}

class EmptyListsIgnoreRule : CommonIgnoreRule {
    private val listTypes = listOf(
        PsiReferenceParameterList::class, PsiModifierList::class, PsiReferenceList::class,
        PsiTypeParameterList::class, PsiExpressionList::class, PsiParameterList::class,
        PsiExpressionListStatement::class, PsiAnnotationParameterList::class
    )

    override fun isIgnored(node: PsiElement): Boolean =
        (node.children.isEmpty() || node.text == "()") && listTypes.any { it.isInstance(node) }
}
