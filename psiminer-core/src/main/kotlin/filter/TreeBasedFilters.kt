package filter

import psi.PsiNode

/***
 * Filter trees by the number of nodes in it
 * @param minSize: Set the minimum number of nodes in target trees
 * @param maxSize: Set the maximum number of nodes in target trees
 */
class TreeBasedFilters(private val minSize: Int = 0, private val maxSize: Int? = null) : Filter {
    override fun isGoodTree(root: PsiNode): Boolean {
        val treeSize = root.preOrder().size
        return (minSize <= treeSize) && (maxSize == null || treeSize <= maxSize)
    }
}
