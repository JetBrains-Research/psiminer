package psi

import GranularityLevel
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import labelextractor.LabeledTree
import psi.language.LanguageHandler
import psi.transformations.PsiTreeTransformation
import psi.transformations.excludenode.ExcludeWhiteSpaceTransformation

class Parser(
    private val languageHandler: LanguageHandler,
    private val psiTreeTransformations: List<PsiTreeTransformation>
) {

    val language = languageHandler.language
    private val isWhiteSpaceHidden = psiTreeTransformations.any { it is ExcludeWhiteSpaceTransformation }

    override fun toString(): String =
        "$language parser with " +
                "${psiTreeTransformations.joinToString { it::class.simpleName ?: "" }} tree transformations"

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
                psiTreeTransformations.forEach { ReadAction.run<Exception> { it.transform(psiFile) } }
                languageHandler.splitByGranularity(psiFile, granularity)
                    .mapNotNull { psiElement ->
                            handlePsiFile(psiElement)?.also { if (isWhiteSpaceHidden) it.root.hideWhiteSpaces() }
                    }
                    .forEach { outputCallback(it) }
            }
    }
}
