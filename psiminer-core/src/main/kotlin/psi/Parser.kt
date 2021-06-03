package psi

import GranularityLevel
import Language
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import labelextractor.LabeledTree
import psi.nodeIgnoreRules.CommonIgnoreRule
import psi.nodeIgnoreRules.PsiNodeIgnoreRule
import psi.nodeIgnoreRules.WhiteSpaceIgnoreRule
import psi.nodeProperties.isHidden
import psi.transformation.PsiTreeTransformer

class Parser(
    private val language: Language,
    nodeIgnoreRules: List<PsiNodeIgnoreRule>,
    treeTransformers: List<PsiTreeTransformer>
) {

    private val nodeIgnoreRules = nodeIgnoreRules.filter {
        it is CommonIgnoreRule || language.description.ignoreRuleType.isInstance(it)
    }
    private val isWhiteSpaceHidden = nodeIgnoreRules.any { it is WhiteSpaceIgnoreRule }

    private val treeTransformers = treeTransformers.filter {
        language.description.treeTransformer.isInstance(it)
    }

    override fun toString(): String =
        "$language parser with " +
                "${nodeIgnoreRules.joinToString { it::class.simpleName ?: "" }} ignore rules and " +
                "${treeTransformers.joinToString { it::class.simpleName ?: "" }} tree transformations"

    /***
     * Collect all files from project that correspond to given language
     * Search is based on checking extension of each file
     * @param project: project where run search
     * @return: list of all PSI Files in project that correspond to required language
     * @see PsiFile
     */
    fun parseProject(
        project: Project,
        granularity: GranularityLevel,
        handlePsiFile: (PsiElement) -> LabeledTree?,
        outputCallback: (LabeledTree) -> Unit
    ) {
        ProjectRootManager
            .getInstance(project)
            .contentRoots
            .flatMap { root ->
                VfsUtil.collectChildrenRecursively(root).filter {
                    it.extension in language.extensions && it.canonicalPath != null
                }
            }
            .forEach { file ->
                val psiFile = ReadAction.compute<PsiFile?, Exception> {
                    PsiManager.getInstance(project).findFile(file)
                } ?: return@forEach
                treeTransformers.forEach { it.transform(psiFile) }
                PsiTreeUtil
                    .collectElements(psiFile) { node -> nodeIgnoreRules.any { it.isIgnored(node) } }
                    .forEach { it.isHidden = true }
                psiFile
                    .splitPsiByGranularity(granularity)
                    .mapNotNull { psiElement ->
                        handlePsiFile(psiElement)?.also { if (isWhiteSpaceHidden) it.root.hideWhiteSpaces() }
                    }
                    .forEach { outputCallback(it) }
            }
    }
}
