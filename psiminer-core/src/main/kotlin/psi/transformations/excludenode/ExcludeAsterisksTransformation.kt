package psi.transformations.excludenode

import com.intellij.psi.PsiElement
import psi.transformations.CommonTreeTransformation

class ExcludeAsterisksTransformation : CommonTreeTransformation, ExcludeNodeTransformation() {
    override fun isIgnored(node: PsiElement): Boolean =
        node.toString() == "PsiDocToken:DOC_COMMENT_LEADING_ASTERISKS"
}
