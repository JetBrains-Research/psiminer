package filter

import com.intellij.psi.PsiElement
import psi.language.LanguageHandler

/**
 * Defines interface to validate trees
 */
interface Filter {

    /**
     * Interface method to validate tree
     * @param root: tree to validate
     * @return: `true` if tree is satisfy condition and should be kept and `false` otherwise
     */
    fun validateTree(root: PsiElement, languageHandler: LanguageHandler): Boolean
}
