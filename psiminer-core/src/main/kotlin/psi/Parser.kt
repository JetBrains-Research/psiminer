package psi

import GranularityLevel
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import kotlinx.coroutines.*
import labelextractor.LabeledTree
import psi.language.LanguageHandler
import psi.transformations.CommonTreeTransformation
import psi.transformations.PsiTreeTransformation
import psi.transformations.excludenode.ExcludeWhiteSpaceTransformation
import kotlin.math.ceil

class Parser(
    private val languageHandler: LanguageHandler,
    private val psiTreeTransformations: List<PsiTreeTransformation>
) {

    init {
        psiTreeTransformations.forEach {
            if (!languageHandler.transformationType.isInstance(it) && it !is CommonTreeTransformation) {
                throw IllegalArgumentException("Incorrect transformation ${it::class.simpleName}")
            }
        }
    }

    val language = languageHandler.language
    private val isWhiteSpaceHidden = psiTreeTransformations.any { it is ExcludeWhiteSpaceTransformation }

    override fun toString(): String =
        "$language parser with " +
                "${psiTreeTransformations.joinToString { it::class.simpleName ?: "" }} tree transformations"

    private fun extractPsiFiles(project: Project): List<PsiFile> =
        ProjectRootManager
            .getInstance(project)
            .contentRoots
            .flatMap { root ->
                VfsUtil.collectChildrenRecursively(root).filter {
                    it.extension in language.extensions && it.canonicalPath != null
                }
            }
            .mapNotNull {
                PsiManager.getInstance(project).findFile(it)
            }

    private suspend fun processPsiFilesAsync(
        psiFiles: List<PsiFile>,
        granularity: GranularityLevel,
        handlePsiFile: (PsiElement) -> LabeledTree?,
        outputCallback: (LabeledTree) -> Unit
    ) = coroutineScope {
        psiFiles.map { psiFile ->
            launch(Dispatchers.Default) {
                val labeledTrees = ReadAction.compute<List<LabeledTree>, Exception> {
                    psiTreeTransformations.forEach { it.transform(psiFile) }
                    languageHandler
                        .splitByGranularity(psiFile, granularity)
                        .mapNotNull { psiElement ->
                            handlePsiFile(psiElement)?.also { if (isWhiteSpaceHidden) it.root.hideWhiteSpaces() }
                        }
                }
                labeledTrees.forEach {
                    withContext(Dispatchers.IO) {
                        ReadAction.run<Exception> { outputCallback(it) }
                    }
                }
            }
        }
    }

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
        PsiManager.getInstance(project)
        val psiFiles = extractPsiFiles(project)
        val nBatches = ceil(psiFiles.size.toDouble() / 10_000).toInt()
        psiFiles.chunked(10_000).forEachIndexed { index, batch ->
            println("Processing batch ${index + 1}/$nBatches")
            runBlocking { processPsiFilesAsync(batch, granularity, handlePsiFile, outputCallback) }
        }
    }
}
