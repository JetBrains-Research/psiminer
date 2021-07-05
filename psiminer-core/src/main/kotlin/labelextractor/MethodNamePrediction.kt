package labelextractor

import GranularityLevel
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import psi.language.LanguageHandler
import psi.nodeProperties.technicalToken

class MethodNamePrediction : LabelExtractor() {

    override val granularityLevel = GranularityLevel.Method

    override fun handleTree(root: PsiElement, languageHandler: LanguageHandler): String? {
        val methodName = languageHandler.methodProvider.getName(root) ?: return null
        // Mark all occurrences in subtree with METHOD_NAME token
        PsiTreeUtil
            .collectElements(root) { it.textMatches(methodName) }
            .forEach { it.technicalToken = METHOD_NAME }
        // TODO: implement text replacement of original method name to special token
        // For now, label extractor is working under ReadAction and cannot be applied to write changes
        // root.renameAllSubtreeOccurrences(METHOD_NAME) // Rename text representation
        return methodName
    }

    private companion object {
        const val METHOD_NAME = "METHOD_NAME"
    }
}
