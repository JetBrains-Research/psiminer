package labelextractor

import astminer.common.splitToSubtokens
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.util.PsiTreeUtil
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.merge
import org.jetbrains.kotlin.idea.codeInliner.CommentHolder.CommentNode.Companion.mergeComments
import psi.language.LanguageHandler

class MethodCommentLabelExtractor : LabelExtractor() {
    override val granularityLevel = GranularityLevel.Method

    override fun handleTree(root: PsiElement, languageHandler: LanguageHandler): Label? {
        val methodCommentNodes = PsiTreeUtil.collectElementsOfType(root, PsiComment::class.java)

        val methodDocComments = methodCommentNodes.filterIsInstance<PsiDocComment>()
            .map { it.descriptionElements }.flatMap { it.map { psiElement -> psiElement.text } }
        val methodComments = methodCommentNodes.filter { it !is PsiDocComment }.map { it.text }
        val onlyCommentsText = methodComments + methodDocComments

        val filteredCommentLabel = onlyCommentsText.flatMap { splitToSubtokens(it) }
            .filterNot { (it == "/*") || (it == "*/") || (it.none { c -> c.isLetterOrDigit() }) }.joinToString("|")
        return if (filteredCommentLabel.isEmpty()) {
            null
        } else {
            StringLabel(filteredCommentLabel);
        }
    }
}