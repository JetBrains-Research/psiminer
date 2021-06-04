package labelextractor

import GranularityLevel
import Language
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiElement

data class LabeledTree(val root: PsiElement, val label: String)

/**
 * Base class for label extracting logic, previous known as `Problem` interface
 * @property granularityLevel: define level of granularity
 * @see GranularityLevel
 */
abstract class LabelExtractor {
    abstract val granularityLevel: GranularityLevel

    /**
     * Custom function to define the way to extract label from tree
     * @param root: Root of PSI Tree where label should be extracted
     * @return string label of tree or null if label can not be extracted
     */
    protected abstract fun handleTree(root: PsiElement, language: Language): String?

    /**
     * Interface method to extract label from tree.
     * Run handling in thread safe way.
     * @param root: Root of PSI Tree where label should be extracted
     * @return tree with label or null if label can not be extracted
     * @see LabeledTree
     */
    fun extractLabel(root: PsiElement, language: Language): LabeledTree? =
        ReadAction.compute<LabeledTree?, Exception> {
            handleTree(root, language)?.let { LabeledTree(root, it) }
        }
}
