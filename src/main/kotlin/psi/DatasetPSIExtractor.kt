package psi

import Config
import Dataset
import DatasetStatistic
import ExtractingStatistic
import TreeConstants.methodNameToken
import astminer.common.getNormalizedToken
import astminer.common.setNormalizedToken
import astminer.paths.PathMiner
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import getTreeSize
import groupPathsByResolvedTypes
import kotlinx.coroutines.*
import storage.XLabeledPathContexts
import storage.XPathContext
import storage.XPathContextsStorage
import java.io.File
import kotlin.math.ceil

class DatasetPSIExtractor(private val storage: XPathContextsStorage<String>, private val miner: PathMiner) {

    fun extractPsiFromDataset(datasetPath: String): DatasetStatistic {
        val datasetStatistic = DatasetStatistic()

        val datasetFile = File(datasetPath)
        Dataset.values().forEach { holdout ->
            val holdoutFile = datasetFile.resolve(holdout.folderName)
            val holdoutProjects = holdoutFile.walk().maxDepth(1).filter { it.name != holdout.folderName }.toList()
            val holdoutProjectsStatistic = holdoutProjects.mapNotNull { projectPath ->
                val project =
                    ProjectUtil.openOrImport(projectPath.path, null, true) ?: return@mapNotNull null
                extractPsiFromProject(project, holdout)
            }
            holdoutProjectsStatistic.forEach { datasetStatistic.addProjectStatistic(holdout, it) }
        }
        return datasetStatistic
    }

    private fun extractPsiFromProject(project: Project, dataset: Dataset): ExtractingStatistic {
        val extractingStatistic = ExtractingStatistic()
        println("Extract PSI from $dataset.${project.name}...")
        val nPathContexts = if (dataset == Dataset.Train) Config.maxPathsInTrain else Config.maxPathsInTest

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

        extractingStatistic.nFiles += projectPsiFiles.size
        val nBatches = ceil(projectPsiFiles.size.toDouble() / Config.batchSize).toInt()
        projectPsiFiles.chunked(Config.batchSize).forEachIndexed { batch_idx, batch ->
            println("Process batch ${batch_idx + 1}/$nBatches")
            runBlocking {
                extractPathsForEachPsi(batch, nPathContexts).forEach {
                    extractingStatistic.nSamples += it.size
                    extractingStatistic.nPaths += it.map { sample -> sample.xPathContexts.size }.sum()
                    it.forEach { sample -> storage.store(sample, dataset) }
                }
            }
        }

        return extractingStatistic
    }

    private suspend fun extractPathsForEachPsi(psiFiles: List<PsiFile>, nPathContexts: Int?) = coroutineScope {
        val deferred = psiFiles.map {
            async(Dispatchers.Default) {
                ReadAction.compute<List<XLabeledPathContexts<String>>, Throwable> {
                    extractPathsFromPsiFile(it, nPathContexts)
                }
            }
        }
        deferred.awaitAll()
    }

    private fun extractPathsFromPsiFile(psiFile: PsiFile, nPathContexts: Int?): List<XLabeledPathContexts<String>> {
        val rootNode = TreeBuilder().convertPSITree(psiFile)
        val methods = PsiMethodSplitter().splitIntoMethods(rootNode)
        return methods.map { methodInfo ->
            val methodNameNode = methodInfo.method.nameNode ?: return@map null
            val methodRoot = methodInfo.method.root

            val methodSize = getTreeSize(methodRoot)
            if (Config.maxTreeSize != null && Config.maxTreeSize < methodSize) {
                return@map null
            }

            val label = methodNameNode.getNormalizedToken()
            if (label == "") return@map null
            if (Config.hideMethodName) {
                methodNameNode.setNormalizedToken(methodNameToken)
            }

//            printTree(methodRoot, true)

            // Retrieve paths from every node individually
            val allPaths = miner.retrievePaths(methodRoot).shuffled()
            val paths = if (Config.resolvedTypesFirst) groupPathsByResolvedTypes(allPaths, nPathContexts)
            else allPaths.let { it.take(nPathContexts ?: it.size) }
            XLabeledPathContexts(label, paths.map { XPathContext.createFromASTPath(it) })
        }.filterNotNull()
    }
}
