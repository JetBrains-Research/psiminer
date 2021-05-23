package psi.nodeIgnoreRules

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace

class WhiteSpaceIgnoreRule : PsiNodeIgnoreRule {
    override fun isIgnored(node: PsiElement): Boolean = node is PsiWhiteSpace
}
