package labelextractor

import com.intellij.psi.PsiElement
import com.intellij.psi.javadoc.PsiDocComment
import org.jetbrains.kotlin.kdoc.psi.impl.KDocImpl
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import psi.language.LanguageHandler

class MethodCommentLabelExtractor(private val onlyDoc: Boolean = true) : LabelExtractor() {
    override val granularityLevel = GranularityLevel.Method

    override fun handleTree(root: PsiElement, languageHandler: LanguageHandler): Label? {

        val docComment = languageHandler.methodProvider.getDocComment(root)

        val nonAlphaNumericRegex = Regex("[^A-Za-z\\d]")

        val docCommentText = if (docComment == null) {
            ArrayList()
        } else {
            when (languageHandler.language) {
                Language.Java -> {
                    (docComment as PsiDocComment).descriptionElements.map { it.text }
                        .flatMap { it.split(nonAlphaNumericRegex) }
                }
                Language.Kotlin -> {
                    (docComment as KDocImpl).getDefaultSection().getContent().split(nonAlphaNumericRegex)
                }
            }
        }
        val extendedLabel = if (onlyDoc) {
            docCommentText
        } else {
            docCommentText + languageHandler.methodProvider.getNonDocComments(root)
                .flatMap { it.text.split(nonAlphaNumericRegex) }
        }.filterNot { it.none { c -> c.isLetterOrDigit() } }.joinToString("|") { it.toLowerCaseAsciiOnly() }
        return if (extendedLabel.isEmpty()) {
            null
        } else {
            StringLabel(extendedLabel)
        }
    }
}
