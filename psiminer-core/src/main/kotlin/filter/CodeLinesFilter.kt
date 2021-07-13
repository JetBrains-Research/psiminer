package filter

import com.intellij.psi.PsiElement
import getCleanCode
import psi.language.LanguageHandler

/***
 * Filter code snippets based on number of lines in it.
 * Corresponded code is normalized to contain only meaningful lines.
 * @param minCodeLines: Set the minimum number of lines in corresponded code snippet
 * @param maxCodeLines: Set the maximum number of lines in corresponded code snippet
 */
class CodeLinesFilter(private val minCodeLines: Int = 0, private val maxCodeLines: Int? = null) : Filter {
    override fun validateTree(root: PsiElement, languageHandler: LanguageHandler): Boolean {
        val cleanCodeLines = getCleanCode(root.text)
        return (minCodeLines <= cleanCodeLines.size) && (maxCodeLines == null || cleanCodeLines.size <= maxCodeLines)
    }
}
