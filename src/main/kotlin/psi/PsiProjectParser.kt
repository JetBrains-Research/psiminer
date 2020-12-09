package psi

import Config
import Dataset
import GranularityLevel
import astminer.common.preOrder
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import kotlinx.coroutines.*
import kotlin.math.ceil

class PsiProjectParser(
    private val granularityLevel: GranularityLevel,
    private val config: Config,
    private val storeCallback: (PsiNode, Dataset) -> Unit
) {
    private val treeBuilder = PsiTreeBuilder(config)

    fun extractPsiFromProject(projectPath: String, holdout: Dataset) {
        val project = ProjectUtil.openOrImport(projectPath, null, true) ?: return
        println("Start parsing $holdout.${project.name} project")
        val projectPsiFiles = mutableListOf<PsiFile>()
        ProjectRootManager.getInstance(project).contentRoots.mapNotNull { root ->
            VfsUtilCore.iterateChildrenRecursively(root, null) { virtualFile ->
                if (virtualFile.extension != "java" || virtualFile.canonicalPath == null) {
                    return@iterateChildrenRecursively true
                }
                val psi =
                    PsiManager.getInstance(project).findFile(virtualFile) ?: return@iterateChildrenRecursively true
                projectPsiFiles.add(psi)
            }
        }

        val nBatches = ceil(projectPsiFiles.size.toDouble() / config.batchSize).toInt()
        projectPsiFiles.chunked(config.batchSize).forEachIndexed { batch_idx, batch ->
            println("Process batch ${batch_idx + 1}/$nBatches")
            runBlocking {
                convertPsiFilesToTrees(batch).flatten().forEach { storeCallback(it, holdout) }
            }
        }
    }

    private suspend fun convertPsiFilesToTrees(psiFiles: List<PsiFile>) = coroutineScope {
        val deferred = psiFiles.map {
            async(Dispatchers.Default) {
                ReadAction.compute<List<PsiNode>, Throwable> {
                    val fileTree = treeBuilder.buildPsiTree(it)
                    when (granularityLevel) {
                        GranularityLevel.File -> listOf(fileTree)
                        GranularityLevel.Method -> fileTree.preOrder()
                            .filter { node -> node.getTypeLabel() == methodNode }
                            .map { node -> node as PsiNode }
                    }
                }
            }
        }
        deferred.awaitAll()
    }

    companion object {
        private const val methodNode = "METHOD"
    }
}
