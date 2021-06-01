package psi

import Language
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import psi.nodeIgnoreRules.CommonIgnoreRule
import psi.nodeIgnoreRules.PsiNodeIgnoreRule
import psi.nodeIgnoreRules.WhiteSpaceIgnoreRule
import psi.nodeProperties.isHidden
import psi.treeProcessors.PsiTreeProcessor

class Parser(
    nodeIgnoreRules: List<PsiNodeIgnoreRule>,
    treeProcessors: List<PsiTreeProcessor>,
    private val language: Language
) {

    private val validatedIgnoreRules = nodeIgnoreRules.filter {
        if (it is CommonIgnoreRule) return@filter true
        if (!language.description.ignoreRuleType.isInstance(it)) {
            println("$language doesn't support ${it::class.simpleName} and thus wouldn't use it")
            return@filter false
        }
        true
    }

    private val validatedTreeProcessors = treeProcessors.filter {
        if (!language.description.treeProcessor.isInstance(it)) {
            println("$language doesn't support ${it::class.simpleName} and thus wouldn't use it")
            return@filter false
        }
        true
    }

    private fun hideNodes(root: PsiElement) =
        PsiTreeUtil
            .collectElements(root) { node -> validatedIgnoreRules.any { it.isIgnored(node) } }
            .forEach { it.isHidden = true }

    private fun processTree(root: PsiElement) =
        validatedTreeProcessors.forEach { it.process(root) }

    private fun handleVirtualFile(virtualFile: VirtualFile, projectCtx: Project) =
        PsiManager.getInstance(projectCtx)
            .findFile(virtualFile)
            ?.also { processTree(it) }
            ?.also { hideNodes(it) }

    fun <R> parseFile(virtualFile: VirtualFile, projectCtx: Project, psiFileHandler: (PsiFile) -> R?) =
        WriteCommandAction.writeCommandAction(projectCtx).compute<PsiFile?, Exception> {
            handleVirtualFile(virtualFile, projectCtx)
        }.let { psiFileHandler(it) }

    fun <R> parseFiles(
        virtualFiles: List<VirtualFile>,
        projectCtx: Project,
        handlePsiFile: (PsiFile) -> R?
    ) = virtualFiles.mapNotNull { parseFile(it, projectCtx, handlePsiFile) }

    fun isWhiteSpacesHidden() = validatedIgnoreRules.any { it is WhiteSpaceIgnoreRule }
}
