package psi

import Config
import Dataset
import GranularityLevel
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import filter.Filter
import kotlinx.coroutines.*
import problem.Sample
import kotlin.math.ceil

class PsiProjectParser(
    private val granularityLevel: GranularityLevel,
    private val config: Config,
    private val filters: List<Filter>,
    private val problemCallback: (PsiNode) -> Sample?,
    private val storeCallback: (PsiNode, String, Dataset) -> Unit
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
                convertPsiFilesToTrees(batch).flatten().forEach { storeCallback(it.root, it.label, holdout) }
            }
        }
    }

    private suspend fun convertPsiFilesToTrees(psiFiles: List<PsiFile>) = coroutineScope {
        val deferred = psiFiles.map {
            async(Dispatchers.Default) {
                ReadAction.compute<List<Sample>, Throwable> {
                    val fileTree = treeBuilder.buildPsiTree(it)
                    when (granularityLevel) {
                        GranularityLevel.File -> listOf(fileTree)
                        GranularityLevel.Class -> fileTree.preOrder().filter { it.wrappedNode is PsiClass }
                        GranularityLevel.Method -> fileTree.preOrder().filter { it.wrappedNode is PsiMethod }
                    }.filter { root -> filters.all { it.isGoodTree(root) } }.mapNotNull { problemCallback(it) }
                }
            }
        }
        deferred.awaitAll()
    }
}
