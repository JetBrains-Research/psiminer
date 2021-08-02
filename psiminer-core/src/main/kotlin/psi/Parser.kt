package psi

import GranularityLevel
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
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

    /**
     * Collect all files from project that correspond to given language
     * Search is based on checking extension of each file
     * @param project: project where run search
     * @return: list of all Virtual Files in project that correspond to required language.
     * @see VirtualFile
     */
    private fun extractProjectFiles(project: Project): List<VirtualFile> =
        ProjectRootManager
            .getInstance(project)
            .contentRoots
            .flatMap { root ->
                VfsUtil.collectChildrenRecursively(root).filter {
                    it.extension in language.extensions && it.canonicalPath != null
                }
            }

    private fun processVirtualFile(virtualFile: VirtualFile, psiManager: PsiManager, callback: (PsiElement) -> Any?) {
        val psiFile = psiManager.findFile(virtualFile) ?: return
        psiTreeTransformations.forEach { it.transform(psiFile) }
        val granularityPsiElements = languageHandler.splitByGranularity(psiFile, granularity)
        granularityPsiElements.forEach { callback(it) }
    }

    private suspend fun processVirtualFilesAsync(
        psiManager: PsiManager,
        virtualFiles: List<VirtualFile>,
        callback: (PsiElement) -> Any?
    ) = coroutineScope {
        virtualFiles.map { virtualFile ->
            launch {
                ReadAction.run<Exception> {
                    processVirtualFile(virtualFile, psiManager, callback)
                }
            }
        }
    }

    fun parseProjectAsync(project: Project, batchSize: Int?, callback: (PsiElement) -> Any?) {
        val psiManager = PsiManager.getInstance(project)
        val virtualFiles = extractProjectFiles(project)
        val trueBatchSize = batchSize ?: virtualFiles.size
        val nBatches = ceil(virtualFiles.size.toDouble() / trueBatchSize).toInt()
        virtualFiles.chunked(trueBatchSize).forEachIndexed { index, batch ->
            println("Processing batch ${index + 1}/$nBatches")
            runBlocking { processVirtualFilesAsync(psiManager, batch, callback) }
        }
    }

    fun parseProject(project: Project, callback: (PsiElement) -> Any?) {
        val psiManager = PsiManager.getInstance(project)
        val virtualFiles = extractProjectFiles(project)
        virtualFiles.forEachIndexed { index, virtualFile ->
            if (index % 10_000 == 0 && index != 0) println("Done with ${index + 1} out of ${virtualFiles.size} files.")
            processVirtualFile(virtualFile, psiManager, callback)
        }
    }
}
