import astminer.storage.MetaDataStorage
import astminercompatibility.store
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import filter.Filter
import labelextractor.LabelExtractor
import labelextractor.LabeledTree
import me.tongfei.progressbar.ProgressBar
import org.slf4j.LoggerFactory
import psi.Parser
import psi.ParserException
import psi.language.JavaHandler
import psi.language.KotlinHandler
import psi.language.PhpHandler
import psi.printTree
import psi.transformations.PsiTreeTransformation
import storage.Storage
import storage.StoragesManager
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import kotlin.concurrent.thread
import kotlin.io.path.Path
import kotlin.system.measureTimeMillis

class Pipeline(
    val language: Language,
    private val repositoryOpener: RepositoryOpener,
    val psiTreeTransformations: List<PsiTreeTransformation>,
    private val filters: List<Filter>,
    val labelExtractor: LabelExtractor,
    val storagesManager: StoragesManager,
    val collectMetadata: Boolean = false
) {
    //private var metaDataStorage: MetaDataStorage? = null

    private val logger = LoggerFactory.getLogger(javaClass)

//    private val languageHandler = when (language) {
//        Language.Java -> JavaHandler()
//        Language.Kotlin -> KotlinHandler()
//        Language.PHP -> PhpHandler()
//    }
//
//    private val parser = Parser(languageHandler, psiTreeTransformations, labelExtractor.granularityLevel)

    private fun checkFolderIsDataset(folder: File): Boolean {
        val folderDirNames = folder.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: return false
        return Dataset.values().all { folderDirNames.contains(it.folderName) }
    }

    fun extract(
        inputDirectory: File,
        numThreads: Int = 1,
        printTrees: Boolean = false
    ) {
//        if (collectMetadata) {
//            val metadataPath = Path(storage.outputDirectory.path, "metadata").toString()
//            metaDataStorage = MetaDataStorage(metadataPath)
//        }
        require(numThreads > 0) { "Amount threads must be positive." }
        //println("Parser configuration:\n$parser")
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
        //metaDataStorage?.close()
        //metaDataStorage = null
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
        val filesQueue = LinkedBlockingQueue(extractProjectFiles(project, language))

        val repositoryProcessors = List(numThreads) {
            Thread(
                RepositoryProcessor(
                    storage = storagesManager.createStorage(),
                    collectMetadata = collectMetadata,
                    labelExtractor = labelExtractor,
                    filters = filters,
                    psiTreeTransformations = psiTreeTransformations,
                    language = language,
                    filesQueue, project, holdout, printTrees
                )
            )
        }
        val timeMillis = measureTimeMillis {
            repositoryProcessors.forEach { it.start() }
            repositoryProcessors.forEach { it.join() }
        }
        println("\nProcessed in ${timeMillis/1000}s!\n")
    }

//    private fun processPsiTree(psiRoot: PsiElement, holdout: Dataset? = null, printTrees: Boolean = false): Boolean {
//        if (filters.any { !it.validateTree(psiRoot, languageHandler) }) return false
//        val labeledTree = labelExtractor.extractLabel(psiRoot, languageHandler) ?: return false
//        synchronized(storage) {
//            storage.store(labeledTree, holdout)
//            metaDataStorage?.store(labeledTree, holdout)
//            if (printTrees) labeledTree.root.printTree()
//        }
//        return true
//    }

//    private fun labelTree(psiRoot: PsiElement): LabeledTree? {
//        if (filters.any { !it.validateTree(psiRoot, languageHandler) })
//            return null
//        return labelExtractor.extractLabel(psiRoot, languageHandler)
//    }
//
//    private fun storeTrees(labeledTree: LabeledTree, holdout: Dataset?, printTrees: Boolean) {
//        ReadAction.run<Exception> {
//            storage.store(labeledTree, holdout)
//            metaDataStorage?.store(labeledTree, holdout)
//            if (printTrees) labeledTree.root.printTree()
//        }
//    }
//
//    private fun <T> processProject(
//        project: Project,
//        numThreads: Int = 1,
//        produce: (PsiElement) -> T?,
//        consume: (T) -> Unit
//    ) {
//        val projectFiles = extractProjectFiles(project, language)
//        val taskQueue = LinkedBlockingQueue(projectFiles)

//        val consumer = thread(start = true) {
//            while (!Thread.currentThread().isInterrupted) {
//                try {
//                    val task = taskQueue.take()
//                    consume(task)
//                } catch (e: InterruptedException) {
//                    Thread.currentThread().interrupt()
//                }
//            }
//            val remainingTasks = mutableListOf<T>()
//            taskQueue.drainTo(remainingTasks)
//            remainingTasks.forEach(consume)
//        }
//        val producers = Executors.newFixedThreadPool(numThreads)
//        val progressBar = ProgressBar(project.name, projectFiles.size.toLong())
//        val futures = projectFiles.map { file ->
//            producers.submit {
//                try {
//                    parser.parseFile(file, project, produce, taskQueue)
//                } catch (exception: ParserException) {
//                    logger.error("Error while parsing ${exception.filepath}")
//                } finally {
//                    progressBar.step()
//                }
//            }
//        }
//        producers.shutdown()
//        futures.forEach { it.get() }
//        progressBar.close()
//        consumer.interrupt()

//        val channel = Channel<LabeledTree>(1000)
//        launch {
//            val jobs = projectFiles.map { file ->
//                launch {
//                    parser.parseFile(file, project, { labelTree(it) }, channel)
//                }
//            }
//            jobs.forEach { it.join() }
//            println("closing")
//            channel.close()
//        }
//        launch {
//            storeTrees(holdout, printTrees, channel)
//        }

//        val progressBar = ProgressBar(project.name, projectFiles.size.toLong())
//
//        val service = Executors.newFixedThreadPool(numThreads)
//        val futures = projectFiles.map { file ->
//            service.submit {
//                try {
//                    parser.parseFile(file, project) { processPsiTree(it, holdout, printTrees) }
//                } catch (exception: ParserException) {
//                    logger.error("Error while parsing ${exception.filepath}")
//                } finally {
//                    progressBar.step()
//                }
//            }
//        }
//        service.shutdown()
//        futures.forEach { it.get() }
//        progressBar.close()
//    }
}
