package filter

import kotlinx.serialization.Serializable
import psi.PsiNode

@Serializable
abstract class FilterConfig {
    abstract fun createFilter(): Filter
}

interface Filter {
    fun isGoodTree(root: PsiNode): Boolean
}
