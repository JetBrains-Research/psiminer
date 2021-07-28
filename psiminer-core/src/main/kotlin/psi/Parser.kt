package psi

import GranularityLevel
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import psi.language.LanguageHandler
import psi.transformations.CommonTreeTransformation
import psi.transformations.PsiTreeTransformation
import kotlin.math.ceil

class Parser(
    private val languageHandler: LanguageHandler,
    private val psiTreeTransformations: List<PsiTreeTransformation>,
    private val granularity: GranularityLevel
) {

    init {
        psiTreeTransformations.forEach {
            if (!languageHandler.transformationType.isInstance(it) && it !is CommonTreeTransformation) {
                throw IllegalArgumentException("Incorrect transformation ${it::class.simpleName}")
            }
        }
    }

    val language = languageHandler.language

    override fun toString(): String =
        "$language parser with " +
                "${psiTreeTransformations.joinToString { it::class.simpleName ?: "" }} tree transformations"

    private fun extractProjectFiles(project: Project): List<VirtualFile> =
        ProjectRootManager
            .getInstance(project)
            .contentRoots
            .flatMap { root ->
                VfsUtil.collectChildrenRecursively(root).filter {
                    it.extension in language.extensions && it.canonicalPath != null
                }
            }

    private suspend fun processPsiFilesAsync(
        psiManager: PsiManager,
        virtualFiles: List<VirtualFile>,
        callback: (PsiElement) -> Any?
    ) = coroutineScope {
            virtualFiles.map { virtualFile ->
                launch(Dispatchers.Default) {
                    val psiFile =
                        ReadAction.compute<PsiFile?, Exception> { psiManager.findFile(virtualFile) } ?: return@launch
                    psiTreeTransformations.forEach { ReadAction.run<Exception> { it.transform(psiFile) } }
                    val granularityPsiElements = ReadAction.compute<List<PsiElement>, Exception> {
                        languageHandler.splitByGranularity(psiFile, granularity)
                    }
                    granularityPsiElements.forEach {
                        ReadAction.run<Exception> { callback(it) }
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
    fun parseProjectAsync(project: Project, batchSize: Int, callback: (PsiElement) -> Any?) {
        val psiManager = PsiManager.getInstance(project)
        val virtualFiles = extractProjectFiles(project)
        val nBatches = ceil(virtualFiles.size.toDouble() / batchSize).toInt()
        virtualFiles.chunked(batchSize).forEachIndexed { index, batch ->
            println("Processing batch ${index + 1}/$nBatches")
            runBlocking { processPsiFilesAsync(psiManager, batch, callback) }
        }
    }
}
