import com.intellij.psi.PsiElement
import com.intellij.openapi.project.Project
import filter.Filter
import labelextractor.LabelExtractor
import psi.Parser
import psi.language.JavaHandler
import psi.language.KotlinHandler
import psi.printTree
import psi.transformations.PsiTreeTransformation
import storage.Storage
import java.io.File

class Pipeline(
    val language: Language,
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

    fun extract(
        inputDirectory: File,
        parseAsync: Boolean = false,
        batchSize: Int? = null,
        printTrees: Boolean = false
    ) {
        println("Starting data extraction using the following parser configuration\n$parser")
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
                    processProject(holdoutProjectFile, holdout, parseAsync, batchSize, printTrees)
                }
            }
        } else {
            println("No dataset found. Process all sources under passed path")
            processProject(inputDirectory, null, parseAsync, batchSize, printTrees)
        }
    }

    private fun processProject(
        projectFile: File,
        holdout: Dataset?,
        parseAsync: Boolean = false,
        batchSize: Int? = null,
        printTrees: Boolean = false
    ) {
        // TODO: log why we can't process the project
        val project = openProject(projectFile) ?: return
        applyParserToProject(project, parseAsync, batchSize) { psiRoot: PsiElement ->
            if (filters.any { !it.validateTree(psiRoot, languageHandler) }) return@applyParserToProject false
            val labeledTree = labelExtractor.extractLabel(psiRoot, languageHandler) ?: return@applyParserToProject false
            storage.store(labeledTree, holdout)
            if (printTrees) labeledTree.root.printTree()
            true
        }
//        closeProject(project)
    }

    private fun applyParserToProject(
        project: Project,
        parseAsync: Boolean,
        batchSize: Int?,
        callback: (PsiElement) -> Any
    ) {
        if (parseAsync) parser.parseProjectAsync(project, batchSize, callback)
        else parser.parseProject(project, callback)
    }
}
