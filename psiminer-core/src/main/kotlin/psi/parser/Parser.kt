package psi.parser

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import psi.nodeProperties.isHidden
import psi.nodeIgnoreRules.PsiNodeIgnoreRule

abstract class Parser(private val nodeIgnoreRules: List<PsiNodeIgnoreRule>) {

    abstract val extensions: List<String>

    abstract val psiElementVisitor: PsiElementVisitor

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
