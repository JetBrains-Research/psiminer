package labelextractor

import com.intellij.psi.PsiElement
import psi.language.LanguageHandler

class MethodCommentLabelExtractor(private val onlyDoc: Boolean = true) : LabelExtractor() {
    override val granularityLevel = GranularityLevel.Method

    override fun handleTree(root: PsiElement, languageHandler: LanguageHandler): Label? {

        val docCommentString = languageHandler.methodProvider.getDocCommentString(root)
        val extendedLabel = if (onlyDoc) {
            docCommentString
        } else {
            mutableListOf(
                docCommentString,
                languageHandler.methodProvider.getNonDocCommentsString(root)
            ).joinToString { "|" }
        }
        return if (extendedLabel.isEmpty()) {
            null
        } else {
            StringLabel(extendedLabel)
        }
    }
}
