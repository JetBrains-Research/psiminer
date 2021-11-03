package labelextractor

import GranularityLevel
import com.intellij.psi.PsiElement
import psi.language.LanguageHandler

class CodeCompletionLabelExtractor : LabelExtractor() {

    override val granularityLevel = GranularityLevel.File

    override fun handleTree(root: PsiElement, languageHandler: LanguageHandler) = ""
}
