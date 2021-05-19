package filter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import psi.PsiNode

@Serializable
@SerialName("treeSize")
class TreeSizeFilterConfig(
    val minSize: Int = 0, // Set the minimum number of nodes in target trees
    val maxSize: Int? = null // Set the maximum number of nodes in target trees
) : FilterConfig() {
    override fun createFilter(): Filter = TreeSizeFilter(this)
}

class TreeSizeFilter(private val config: TreeSizeFilterConfig) : Filter {
    override fun isGoodTree(root: PsiNode): Boolean {
        val treeSize = root.preOrder().size
        return (config.minSize <= treeSize) && (config.maxSize == null || treeSize <= config.maxSize)
    }
}
