package filter

import psi.PsiNode

interface Filter {
    fun checkTree(root: PsiNode): Boolean
}