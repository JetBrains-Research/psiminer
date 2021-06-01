import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.project.Project
import filter.Filter
import kotlinx.coroutines.*
import labelextractor.LabelExtractor
import psi.Parser
import psi.hideWhiteSpaces
import psi.nodeIgnoreRules.PsiNodeIgnoreRule
import psi.printTree
import psi.splitPsiByGranularity
import psi.treeProcessors.PsiTreeProcessor
import storage.Storage
import java.io.File

class Pipeline(
    private val filters: List<Filter>,
    private val labelExtractor: LabelExtractor,
    private val storage: Storage,
    private val parameters: PipelineParameters
) {

    data class PipelineParameters(val batchSize: Int, val printTrees: Boolean)

    private fun checkFolderIsDataset(folder: File): Boolean {
        val folderDirNames = folder.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: return false
        return Dataset.values().all { folderDirNames.contains(it.folderName) }
    }

    fun extract(
        inputDirectory: File,
        languages: List<Language>,
        nodeIgnoreRules: List<PsiNodeIgnoreRule>,
        treeProcessors: List<PsiTreeProcessor>,
    ) {
        val languageParsers = languages.associateWith { Parser(nodeIgnoreRules, treeProcessors, it) }

        val isDataset = checkFolderIsDataset(inputDirectory)
        if (isDataset) {
            println("Dataset structure is detected.")
            Dataset.values().forEach { holdout ->
                val holdoutFolder = inputDirectory.resolve(holdout.folderName)
                val holdoutProjects = holdoutFolder
                    .walk().maxDepth(1).toList().filter { it.name != holdout.folderName && !it.isFile }
                holdoutProjects.forEachIndexed { index, holdoutProjectFile ->
                    println(
                        "Process $holdout.${holdoutProjectFile.name} project " +
                                "(${index + 1}/${holdoutProjects.size})"
                    )
                    processProject(holdoutProjectFile, languageParsers, holdout)
                }
            }
        } else {
            println("No dataset found. Process all sources under passed path")
            processProject(inputDirectory, languageParsers, null)
        }
    }

    private fun processProject(projectFile: File, languageParsers: Map<Language, Parser>, holdout: Dataset?) {
        // TODO: log why we can't process the project
        val project = openProject(projectFile) ?: return
        languageParsers.forEach { (language, parser) ->
            val files = getAllFilesByLanguage(project, language)
            println("Found ${files.size} $language files")
            files.chunked(parameters.batchSize).forEachIndexed { batchId, batchFiles ->
                runBlocking {
                    val labeledTrees = processPsiTreeAsync(batchFiles, project, parser)
                    labeledTrees.filterNotNull().forEach {
                        storage.store(it, holdout, language)
                        if (parameters.printTrees) it.root.printTree()
                    }
                }
            }
        }
//        closeProject(project)
    }

    private suspend fun processPsiTreeAsync(
        files: List<VirtualFile>,
        projectCtx: Project,
        parser: Parser
    ) = coroutineScope {
        files.map {
            async(Dispatchers.Default) {
                parser.parseFile(it, projectCtx) { psiRoot ->
                    psiRoot.splitPsiByGranularity(labelExtractor.granularityLevel).map { psiTreeRoot ->
                        if (filters.all { it.validateTree(psiTreeRoot) }) {
                            labelExtractor.extractLabel(psiTreeRoot)
                                ?.also { if (parser.isWhiteSpacesHidden()) it.root.hideWhiteSpaces() }
                        } else null
                    }
                }
            }
        }.awaitAll().filterNotNull().flatten()
    }
}
