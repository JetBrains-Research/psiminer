package psi.transformations.excludenode

import com.intellij.psi.*
import psi.transformations.CommonTreeTransformation

class ExcludeWhiteSpaceTransformation : CommonTreeTransformation, ExcludeNodeTransformation() {
    override fun isIgnored(node: PsiElement): Boolean = node is PsiWhiteSpace
}
