package psi

import Language
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import psi.nodeProperties.isHidden
import psi.nodeIgnoreRules.PsiNodeIgnoreRule
import psi.nodeIgnoreRules.WhiteSpaceIgnoreRule

class Parser(private val nodeIgnoreRules: List<PsiNodeIgnoreRule>) {

    fun isWhiteSpaceHidden() = nodeIgnoreRules.find { it is WhiteSpaceIgnoreRule } != null

    fun hideAllWhiteSpaces(root: PsiElement) =
        PsiTreeUtil.collectElementsOfType(root, PsiWhiteSpace::class.java).forEach { it.isHidden = true}

    private fun validateNode(node: PsiElement) {
        if (nodeIgnoreRules.any { it.isIgnored(node) }) node.isHidden = true
    }

    private fun hideNodes(root: PsiElement) =
        PsiTreeUtil.collectElements(root) { node -> nodeIgnoreRules.any { it.isIgnored(node)} }
            .forEach { it.isHidden = true }

    fun parseProject(project: Project, language: Language): List<PsiElement> {
        val projectPsiFiles = mutableListOf<PsiFile>()
        ProjectRootManager.getInstance(project).contentRoots.mapNotNull { root ->
            VfsUtilCore.iterateChildrenRecursively(root, null) { virtualFile ->
                if (virtualFile.extension !in language.extensions || virtualFile.canonicalPath == null) {
                    return@iterateChildrenRecursively true
                }
                val psi =
                    PsiManager.getInstance(project).findFile(virtualFile) ?: return@iterateChildrenRecursively true
                projectPsiFiles.add(psi)
            }
        }

        projectPsiFiles.forEach { hideNodes(it) }
        return projectPsiFiles
    }
}
