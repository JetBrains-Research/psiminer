package psi.nodeIgnoreRules

import com.intellij.psi.PsiElement

/***
 * Common interface for all node ignore rules
 */
interface PsiNodeIgnoreRule {

    /***
     * Function to check whenever node should be ignored in PSI tree or not.
     * @param node: node to check
     * @return: `true` if node should be ignored
     */
    fun isIgnored(node: PsiElement): Boolean
}
