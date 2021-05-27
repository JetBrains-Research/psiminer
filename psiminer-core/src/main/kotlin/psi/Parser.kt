package psi

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import psi.language.LanguageDescription
import psi.nodeIgnoreRules.CommonIgnoreRule
import psi.nodeIgnoreRules.PsiNodeIgnoreRule
import psi.nodeIgnoreRules.WhiteSpaceIgnoreRule
import psi.nodeProperties.isHidden

class Parser(
    nodeIgnoreRules: List<PsiNodeIgnoreRule>,
    private val languageDescription: LanguageDescription
) {

    private val validatedIgnoreRules = nodeIgnoreRules.filter {
        it is CommonIgnoreRule || languageDescription.ignoreRuleType.isInstance(it)
    }

    private fun hideNodes(root: PsiElement) =
        PsiTreeUtil
            .collectElements(root) { node -> validatedIgnoreRules.any { it.isIgnored(node) } }
            .forEach { it.isHidden = true }

    fun parseFile(virtualFile: VirtualFile, projectCtx: Project): PsiElement? {
        val psiFile = PsiManager.getInstance(projectCtx).findFile(virtualFile) ?: return null
        hideNodes(psiFile)
        return psiFile
    }

    fun parseFiles(virtualFiles: List<VirtualFile>, projectCtx: Project): List<PsiElement> =
        virtualFiles.mapNotNull { parseFile(it, projectCtx) }

    /***
     * Some modifications during filtering or label extracting may add whitespaces (e.g. after renaming identifiers)
     * Therefore someone need to hide them manually before saving
     * @param root: tree for which we need to hide white spaces
     * @return: false if parser wasn't initialized with white space ignore rule and true otherwise
     */
    fun hideWhitespaces(root: PsiElement): Boolean {
        if (validatedIgnoreRules.find { it is WhiteSpaceIgnoreRule } == null) return false
        PsiTreeUtil.collectElementsOfType(root, PsiWhiteSpace::class.java).forEach { it.isHidden = true }
        return true
    }
}
