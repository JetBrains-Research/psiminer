package filter

import psi.PsiNode

class TreeSizeFilter(private val minSize: Int = 0, private val maxSize: Int? = null) : Filter {
    override fun isGoodTree(root: PsiNode): Boolean {
        val treeSize = root.preOrder().size
        return (minSize <= treeSize) && (maxSize == null || treeSize <= maxSize)
    }
    companion object {
        const val name = "tree size"
    }
}
