package filter

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

/***
 * Filter trees by the number of nodes in it
 * @param minSize: Set the minimum number of nodes in target trees
 * @param maxSize: Set the maximum number of nodes in target trees
 */
class TreeSizeFilter(private val minSize: Int = 0, private val maxSize: Int? = null) : Filter {
    override fun isGoodTree(root: PsiElement): Boolean {
        val treeSize = PsiTreeUtil.collectElementsOfType(root, PsiElement::class.java).size
        return (minSize <= treeSize) && (maxSize == null || treeSize <= maxSize)
    }
}
