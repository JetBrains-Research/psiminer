package labelextractor

import GranularityLevel
import com.intellij.psi.PsiElement
import psi.language.LanguageHandler

class DummyLabelExtractor(override val granularityLevel: GranularityLevel = GranularityLevel.File) : LabelExtractor() {

    override fun handleTree(root: PsiElement, languageHandler: LanguageHandler) = ""
}
