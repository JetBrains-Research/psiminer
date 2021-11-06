package filter

import com.intellij.psi.PsiElement
import psi.language.LanguageHandler
import psi.treeSize

/***
 * Filter trees by the number of nodes in it
 * @param minSize: Set the minimum number of nodes in target trees
 * @param maxSize: Set the maximum number of nodes in target trees
 */
class TreeSizeFilter(private val minSize: Int = 0, private val maxSize: Int? = null) : Filter {
    override fun validateTree(root: PsiElement, languageHandler: LanguageHandler): Boolean {
        val treeSize = root.treeSize()
        return (minSize <= treeSize) && (maxSize == null || treeSize <= maxSize)
    }
}
