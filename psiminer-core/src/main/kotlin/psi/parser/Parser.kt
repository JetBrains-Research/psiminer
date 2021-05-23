package psi.parser

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import psi.nodeProperties.isHidden
import psi.nodeIgnoreRules.PsiNodeIgnoreRule
import psi.nodeIgnoreRules.WhiteSpaceIgnoreRule

abstract class Parser(private val nodeIgnoreRules: List<PsiNodeIgnoreRule>) {

    abstract val extensions: List<String>

    abstract val psiElementVisitor: PsiElementVisitor

    fun isWhiteSpaceHidden() = nodeIgnoreRules.find { it is WhiteSpaceIgnoreRule } != null

    fun hideAllWhiteSpaces(root: PsiElement) =
        PsiTreeUtil.collectElementsOfType(root, PsiWhiteSpace::class.java).forEach { it.isHidden = true}

    protected fun validateNode(node: PsiElement) {
        if (nodeIgnoreRules.any { it.isIgnored(node) }) node.isHidden = true
    }

    private fun processPsiTrees(psiTrees: List<PsiElement>) =
        psiTrees.forEach { it.accept(psiElementVisitor) }

    fun parseProject(project: Project): List<PsiElement> {
        val projectPsiFiles = mutableListOf<PsiFile>()
        ProjectRootManager.getInstance(project).contentRoots.mapNotNull { root ->
            VfsUtilCore.iterateChildrenRecursively(root, null) { virtualFile ->
                if (virtualFile.extension !in extensions || virtualFile.canonicalPath == null) {
                    return@iterateChildrenRecursively true
                }
                val psi =
                    PsiManager.getInstance(project).findFile(virtualFile) ?: return@iterateChildrenRecursively true
                projectPsiFiles.add(psi)
            }
        }

        processPsiTrees(projectPsiFiles)
        return projectPsiFiles
    }
}
