package filter

import psi.PsiNode

interface Filter {
    fun isGoodTree(root: PsiNode): Boolean
}
