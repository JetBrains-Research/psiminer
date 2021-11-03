package pipeline

import Dataset
import Language
import com.intellij.openapi.project.Project
import extractProjectFiles
import filter.Filter
import labelextractor.LabelExtractor
import me.tongfei.progressbar.ProgressBar
import org.slf4j.LoggerFactory
import psi.ParserException
import psi.printTree
import psi.transformations.PsiTreeTransformation
import storage.DatasetStorage
import java.io.File
import kotlin.concurrent.thread

class DatasetPipeline(
    language: Language,
    psiTreeTransformations: List<PsiTreeTransformation>,
    labelExtractor: LabelExtractor,
    filters: List<Filter>,
    private val repositoryOpener: PipelineRepositoryOpener,
    val datasetStorage: DatasetStorage,
) : AbstractPipeline(
    language,
    labelExtractor,
    filters,
    psiTreeTransformations
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private fun checkFolderIsDataset(folder: File): Boolean {
        val folderDirNames = folder.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: return false
        return Dataset.values().all { folderDirNames.contains(it.folderName) }
    }

    fun extract(
        inputDirectory: File,
        numThreads: Int = 1,
        printTrees: Boolean = false
    ) {
        require(numThreads > 0) { "Amount threads must be positive." }
        println("Parser configuration:\n$parser")
        val isDataset = checkFolderIsDataset(inputDirectory)
        if (isDataset) {
            println("Dataset structure is detected.")
            Dataset.values().forEach { holdout ->
                val holdoutFolder = inputDirectory.resolve(holdout.folderName)
                val holdoutRepositories = holdoutFolder
                    .walk().maxDepth(1).toList().filter { it.name != holdout.folderName && !it.isFile }
                holdoutRepositories.forEachIndexed { index, holdoutRepositoryRoot ->
                    println(
                        "Start working with ${holdoutRepositoryRoot.name} from " +
                                "$holdout (${index + 1}/${holdoutRepositories.size})"
                    )
                    processRepository(holdoutRepositoryRoot, holdout, numThreads, printTrees)
                }
            }
        } else {
            println("No dataset found. Process all sources from passed path")
            processRepository(inputDirectory, null, numThreads, printTrees)
        }
    }

    private fun processRepository(
        repositoryRoot: File,
        holdout: Dataset?,
        numThreads: Int = 1,
        printTrees: Boolean = false
    ) {
        repositoryOpener.openRepository(repositoryRoot) { project ->
            logger.warn("Process project ${project.name}")
            println("Successfully opened ${project.name}")
            processProject(project, holdout, numThreads, printTrees)
        }
    }

    private fun processProject(
        project: Project,
        holdout: Dataset?,
        numThreads: Int = 1,
        printTrees: Boolean = false
    ) {
        val projectFiles = extractProjectFiles(project, language)

        val progressBar = ProgressBar(project.name, projectFiles.size.toLong())

        val threads = projectFiles.chunked(numThreads).map { files ->
            thread {
                files.map { file ->
                    try {
                        parser.parseFile(file, project) { psiRoot ->
                            if (filters.any { !it.validateTree(psiRoot, languageHandler) }) return@parseFile
                            val labeledTree =
                                labelExtractor.extractLabel(psiRoot, languageHandler) ?: return@parseFile
                            synchronized(datasetStorage) {
                                datasetStorage.store(labeledTree, holdout)
                                if (printTrees) labeledTree.root.printTree()
                            }
                        }
                    } catch (exception: ParserException) {
                        logger.error("Error while parsing ${exception.filepath}")
                    } finally {
                        progressBar.step()
                    }
                }
            }
        }

        threads.forEach { it.join() }
        progressBar.close()
    }
}
