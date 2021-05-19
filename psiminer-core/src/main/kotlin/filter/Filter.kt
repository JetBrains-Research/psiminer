package filter

import kotlinx.serialization.Serializable
import psi.PsiNode

interface Filter {
    fun isGoodTree(root: PsiNode): Boolean
}
