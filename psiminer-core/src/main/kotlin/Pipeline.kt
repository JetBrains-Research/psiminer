import filter.Filter
import labelextractor.LabelExtractor
import org.jetbrains.research.pluginUtilities.openRepository.RepositoryOpener
import psi.Parser
import psi.language.JavaHandler
import psi.language.KotlinHandler
import psi.printTree
import psi.transformations.PsiTreeTransformation
import storage.Storage
import java.io.File
import org.jetbrains.research.pluginUtilities.preprocessing.PreprocessorManager

class Pipeline(
    val language: Language,
    private val preprocessorManager: PreprocessorManager?,
    private val repositoryOpener: RepositoryOpener,
    psiTreeTransformations: List<PsiTreeTransformation>,
    private val filters: List<Filter>,
    val labelExtractor: LabelExtractor,
    val storage: Storage
) {

    private val languageHandler = when (language) {
        Language.Java -> JavaHandler()
        Language.Kotlin -> KotlinHandler()
    }

    private val parser = Parser(languageHandler, psiTreeTransformations, labelExtractor.granularityLevel)

    private fun checkFolderIsDataset(folder: File): Boolean {
        val folderDirNames = folder.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: return false
        return Dataset.values().all { folderDirNames.contains(it.folderName) }
    }

    fun extract(inputDirectory: File, batchSize: Int = 10_000, printTrees: Boolean = false) {
        println("Starting data extraction using the following parser configuration\n$parser")
        val isDataset = checkFolderIsDataset(inputDirectory)
        if (isDataset) {
            println("Dataset structure is detected.")
            Dataset.values().forEach { holdout ->
                val holdoutFolder = inputDirectory.resolve(holdout.folderName)
                val holdoutRepositories = holdoutFolder
                    .walk().maxDepth(1).toList().filter { it.name != holdout.folderName && !it.isFile }
                holdoutRepositories.forEachIndexed { index, holdoutRepositoryRoot ->
                    println(
                        "Process $holdout.${holdoutRepositoryRoot.name} project " +
                                "(${index + 1}/${holdoutRepositories.size})"
                    )
                    processRepository(holdoutRepositoryRoot, holdout, batchSize, printTrees)
                }
            }
        } else {
            println("No dataset found. Process all sources under passed path")
            processRepository(inputDirectory, null, batchSize, printTrees)
        }
    }

    private fun processRepository(
        repositoryRoot: File,
        holdout: Dataset?,
        batchSize: Int = 10_000,
        printTrees: Boolean = false
    ) {
        preprocessorManager?.preprocessRepositoryInplace(repositoryRoot)
        repositoryOpener.openRepository(repositoryRoot) { project ->
            parser.parseProjectAsync(project, batchSize) { psiRoot ->
                if (filters.any { !it.validateTree(psiRoot, languageHandler) }) return@parseProjectAsync false
                val labeledTree =
                    labelExtractor.extractLabel(psiRoot, languageHandler) ?: return@parseProjectAsync false
                storage.store(labeledTree, holdout)
                if (printTrees) labeledTree.root.printTree()
                true
            }
        }
    }
}
