package labelextractor

import GranularityLevel
import astminer.common.splitToSubtokens
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import filter.filteredMethod
import psi.language.LanguageHandler

class FileLevelMethodNameLabelExtractor : LabelExtractor() {
    override val granularityLevel = GranularityLevel.File
    override fun handleTree(root: PsiElement, languageHandler: LanguageHandler): Label? {
        val methods = PsiTreeUtil.collectElementsOfType(root, languageHandler.methodPsiType).filter { method ->
            method.filteredMethod != true
        }
        if (methods.isEmpty()) {
            return null
        }
        val methodNameNodes = methods.map { methodRoot -> languageHandler.methodProvider.getNameNode(methodRoot) }
        val methodNames =
            methodNameNodes.map { methodNameNode -> splitToSubtokens(methodNameNode.text).joinToString("|") }
        return StringLabel(methodNames.joinToString(","))
    }
}
