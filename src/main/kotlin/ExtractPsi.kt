import astminer.common.getNormalizedToken
import astminer.common.preOrder
import astminer.common.setNormalizedToken
import astminer.common.splitToSubtokens
import astminer.paths.PathMiner
import astminer.paths.PathRetrievalSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import me.tongfei.progressbar.ProgressBar
import psi.PsiMethodSplitter
import psi.convertPSITree
import storage.XLabeledPathContexts
import storage.XPathContextsStorage
import java.io.File

fun extractPathsFromFile(psiFile: PsiFile, miner: PathMiner): List<XLabeledPathContexts<String>> {
    val rootNode = convertPSITree(psiFile)
    val methods = PsiMethodSplitter().splitIntoMethods(rootNode)
    return methods.map { methodInfo ->
        val methodNameNode = methodInfo.method.nameNode ?: return@map null
        val methodRoot = methodInfo.method.root
        val label = splitToSubtokens(methodNameNode.getToken()).joinToString("|")
        methodRoot.preOrder().forEach { it.setNormalizedToken() }
        methodNameNode.setNormalizedToken("METHOD_NAME")

        // Retrieve paths from every node individually
        val paths = miner.retrievePaths(methodRoot)
        XLabeledPathContexts(
            label = label,
            xPathContexts = paths.map {
                toXPathContext(
                    path = it,
                    getToken = { node -> node.getNormalizedToken() },
                    getTokenType = { node ->
                        node.getMetadata(Config.psiTypeMetadataKey)?.toString() ?: Config.unknownType
                    }
                )
            }
        )
    }.filterNotNull()
}

fun extractPsiFromProject(project: Project, storage: XPathContextsStorage<String>): DatasetStatistic {
    println("Start extracting psi from ${project.name} project...")
    val miner = PathMiner(PathRetrievalSettings(Config.maxPathHeight, Config.maxPathWidth))

    val datasetStatistic = DatasetStatistic()

    val projectJavaFiles = mutableListOf<VirtualFile>()
    ProjectRootManager.getInstance(project).contentRoots.map { root ->
        VfsUtilCore.iterateChildrenRecursively(root, null) {
            if (it.extension == "java" && it.canonicalPath != null) {
                projectJavaFiles.add(it)
            }
            true
        }
    }

    val absoluteProjectPath = File(project.basePath ?: "")
    ProgressBar.wrap(projectJavaFiles, "Extract PSI").forEach { vFile ->
        val relativePath = File(vFile.canonicalPath ?: "").relativeTo(absoluteProjectPath)
        val dataset = Dataset.valueOf(relativePath.path.split(File.separator)[0].capitalize())
        val psi = PsiManager.getInstance(project).findFile(vFile)
        val extractedPaths =
            psi?.let { extractPathsFromFile(it, miner) } ?: return@forEach
        datasetStatistic.addFileStatistic(dataset, extractedPaths.size)
        extractedPaths.forEach { storage.store(it, dataset) }
    }

    storage.close()
    return datasetStatistic
}
