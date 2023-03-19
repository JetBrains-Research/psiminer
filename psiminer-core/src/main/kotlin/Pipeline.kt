import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import filter.Filter
import labelextractor.LabelExtractor
import org.slf4j.LoggerFactory
import psi.language.JavaHandler
import psi.language.KotlinHandler
import psi.language.PhpHandler
import psi.transformations.PsiTreeTransformation
import storage.StorageManager
import java.io.File
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.system.measureTimeMillis

class Pipeline(
    val language: Language,
    private val repositoryOpener: RepositoryOpener,
    val psiTreeTransformations: List<PsiTreeTransformation>,
    private val filters: List<Filter>,
    val labelExtractor: LabelExtractor,
    val storageManager: StorageManager,
    val collectMetadata: Boolean = false
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val languageHandler = when (language) {
        Language.Java -> JavaHandler()
        Language.Kotlin -> KotlinHandler()
        Language.PHP -> PhpHandler()
    }

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
            true // TODO: change to returning whether process succeeded
        }
    }

    private fun processProject(
        project: Project,
        holdout: Dataset?,
        numThreads: Int = 1,
        printTrees: Boolean = false
    ) {
       val filesQueue: BlockingQueue<VirtualFile> = LinkedBlockingQueue(extractProjectFiles(project, language))

        val workers = List(numThreads) {
            Thread(
                Worker(
                    languageHandler = languageHandler,
                    psiTreeTransformations = psiTreeTransformations,
                    filters = filters,
                    labelExtractor = labelExtractor,
                    storage = storageManager.createStorage(),
                    collectMetadata = collectMetadata,
                    filesQueue = filesQueue,
                    project = project,
                    holdout = holdout,
                    printTrees = printTrees
                )
            )
        }
        val millis = measureTimeMillis {
            workers.forEach { it.start() }
            workers.forEach { it.join() }
        }
        println("Processed ${project.name} in ${millis/1000}s")
    }
}
