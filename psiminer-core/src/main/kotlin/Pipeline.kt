import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.psi.PsiManager
import filter.Filter
import labelextractor.LabelExtractor
import me.tongfei.progressbar.ProgressBar
import org.slf4j.LoggerFactory
import psi.Parser
import psi.ParserException
import psi.language.JavaHandler
import psi.language.KotlinHandler
import psi.nodeProperties.resetRegisteredPropertyDelegates
import psi.printTree
import psi.transformations.PsiTreeTransformation
import storage.Storage
import java.io.File
import kotlin.concurrent.thread

class Pipeline(
    val language: Language,
    psiTreeTransformations: List<PsiTreeTransformation>,
    private val filters: List<Filter>,
    val labelExtractor: LabelExtractor,
    val storage: Storage
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val languageHandler = when (language) {
        Language.Java -> JavaHandler()
        Language.Kotlin -> KotlinHandler()
    }

    private val parser = Parser(languageHandler, psiTreeTransformations, labelExtractor.granularityLevel)

    private fun checkFolderIsDataset(folder: File): Boolean {
        val folderDirNames = folder.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: return false
        return Dataset.values().all { folderDirNames.contains(it.folderName) }
    }

    fun extract(
        inputDirectory: File,
        batchSize: Int = 1,
        printTrees: Boolean = false
    ) {
        require(batchSize > 0) { "Amount threads must be positive." }
        println("Parser configuration:\n$parser")
        val isDataset = checkFolderIsDataset(inputDirectory)
        if (isDataset) {
            println("Dataset structure is detected.")
            Dataset.values().forEach { holdout ->
                val holdoutFolder = inputDirectory.resolve(holdout.folderName)
                val holdoutProjects = holdoutFolder
                    .walk().maxDepth(1).toList().filter { it.name != holdout.folderName && !it.isFile }
                holdoutProjects.forEachIndexed { index, holdoutProjectFile ->
                    println(
                        "Process ${holdoutProjectFile.name} from $holdout (${index + 1}/${holdoutProjects.size})"
                    )
                    processProject(holdoutProjectFile, holdout, batchSize, printTrees)
                }
            }
        } else {
            println("No dataset found. Process all sources under passed path")
            processProject(inputDirectory, null, batchSize, printTrees)
        }
    }

    private fun processProject(
        projectFile: File,
        holdout: Dataset?,
        batchSize: Int = 1,
        printTrees: Boolean = false
    ) {
        val project = openProject(projectFile) ?: return
        logger.warn("Process project ${project.name}")
        val psiManager = PsiManager.getInstance(project)
        val projectFiles = extractProjectFiles(project, language)

        val progressBar = ProgressBar(project.name, projectFiles.size.toLong())

        projectFiles.chunked(batchSize).forEach { files ->
            val threads = files.map { file ->
                thread {
                    try {
                        parser.parseFile(file, psiManager) { psiRoot ->
                            if (filters.any { !it.validateTree(psiRoot, languageHandler) }) return@parseFile
                            val labeledTree =
                                labelExtractor.extractLabel(psiRoot, languageHandler) ?: return@parseFile
                            synchronized(storage) {
                                storage.store(labeledTree, holdout)
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
            threads.forEach { it.join() }
            resetRegisteredPropertyDelegates()
        }

        progressBar.close()
        ProjectManagerEx.getInstanceEx().closeAndDisposeAllProjects(false)
    }
}
