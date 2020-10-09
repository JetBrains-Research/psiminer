package psi

import Config
import Dataset
import DatasetStatistic
import ExtractingStatistic
import astminer.common.preOrder
import astminer.common.setNormalizedToken
import astminer.common.splitToSubtokens
import astminer.paths.PathMiner
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import getTreeSize
import storage.XLabeledPathContexts
import storage.XPathContext
import storage.XPathContextsStorage
import java.io.File

fun extractPathsFromPsiFile(psiFile: PsiFile, miner: PathMiner): List<XLabeledPathContexts<String>> {
    val rootNode = TreeBuilder().convertPSITree(psiFile)
    val methods = PsiMethodSplitter().splitIntoMethods(rootNode)
    return methods.map { methodInfo ->
        val methodNameNode = methodInfo.method.nameNode ?: return@map null
        val methodRoot = methodInfo.method.root

        val methodSize = getTreeSize(methodRoot)
        if (Config.maxTreeSize != null && Config.maxTreeSize < methodSize) {
            return@map null
        }

        val label = splitToSubtokens(methodNameNode.getToken()).joinToString("|")
        methodRoot.preOrder().map {
            it.setNormalizedToken(
                    if (it.getToken().toIntOrNull() == null)
                        splitToSubtokens(it.getToken()).joinToString("|")
                    else
                        it.getToken()
            )
        }
        methodNameNode.setNormalizedToken("METHOD_NAME")

//        printTree(methodRoot, true)

        // Retrieve paths from every node individually
        val paths = miner.retrievePaths(methodRoot)
        XLabeledPathContexts(label, paths.map { XPathContext.createFromASTPath(it) })
    }.filterNotNull()
}

fun extractPsiFromProject(
        project: Project,
        storage: XPathContextsStorage<String>,
        miner: PathMiner,
        dataset: Dataset
): ExtractingStatistic {
    val extractingStatistic = ExtractingStatistic()
    println("Extract PSI from ${project.name}...")
    var fileCounter = 0

    ProjectRootManager.getInstance(project).contentRoots.forEach { root ->
        VfsUtilCore.iterateChildrenRecursively(root, null) { virtualFile ->
            if (virtualFile.extension != "java" || virtualFile.canonicalPath == null) {
                return@iterateChildrenRecursively true
            }
            fileCounter += 1
            val psi = PsiManager.getInstance(project).findFile(virtualFile) ?: return@iterateChildrenRecursively true
            val extractedPaths = extractPathsFromPsiFile(psi, miner)
            extractingStatistic.nFiles += 1
            extractingStatistic.nSamples += extractedPaths.size
            extractingStatistic.nPaths += extractedPaths.map { it.xPathContexts.size }.sum()
            extractedPaths.forEach { storage.store(it, dataset) }
            if (fileCounter % 100 == 0) {
                println("processed $fileCounter files...")
            }
            true
        }
    }

    return extractingStatistic
}

fun extractPsiFromDataset(
    datasetPath: String,
    storage: XPathContextsStorage<String>,
    miner: PathMiner
): DatasetStatistic {
    val datasetStatistic = DatasetStatistic()

    val datasetFile = File(datasetPath)
    Dataset.values().forEach { holdout ->
        println("Extract PSI for $holdout holdout")
        val holdoutFile = datasetFile.resolve(holdout.folderName)
        val holdoutProjects = holdoutFile.walk().maxDepth(1).filter { it.name != holdout.folderName }.toList()
        holdoutProjects.forEach { projectPath ->
            println("Extracting PSI from $projectPath)")
            val project = ProjectUtil.openOrImport(projectPath.path, null, true)
            if (project != null) {
                val extractedProjectStatistic = extractPsiFromProject(project, storage, miner, holdout)
                datasetStatistic.addProjectStatistic(holdout, extractedProjectStatistic)
            }
        }
    }
    return datasetStatistic
}
