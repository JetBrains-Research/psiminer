import filter.Filter
import problem.Problem
import psi.Parser
import psi.nodeIgnoreRules.PsiNodeIgnoreRule
import psi.printTree
import psi.splitPsiByGranularity
import storage.Storage
import java.io.File

class Pipeline(private val filters: List<Filter>, private val problem: Problem, private val storage: Storage) {

    private fun checkFolderIsDataset(folder: File): Boolean {
        val folderDirNames = folder.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: return false
        return Dataset.values().all { folderDirNames.contains(it.folderName) }
    }

    fun extract(
        inputDirectory: File,
        languages: List<Language>,
        nodeIgnoreRules: List<PsiNodeIgnoreRule>,
        printTrees: Boolean = false
    ) {
        val languageParsers = languages.associateWith { Parser(nodeIgnoreRules, it.description) }

        val isDataset = checkFolderIsDataset(inputDirectory)
        if (isDataset) {
            println("Dataset structure is detected.")
            Dataset.values().forEach { holdout ->
                val holdoutFolder = inputDirectory.resolve(holdout.folderName)
                val holdoutProjects = holdoutFolder
                    .walk().maxDepth(1).toList().filter { it.name != holdout.folderName && !it.isFile }
                holdoutProjects.forEachIndexed { index, holdoutProjectFile ->
                    println("Process $holdout.${holdoutProjectFile.name} project " +
                            "(${index + 1}/${holdoutProjects.size})")
                    processProject(holdoutProjectFile, languageParsers, holdout, printTrees)
                }
            }
        } else {
            println("No dataset found. Process all sources under passed path")
            processProject(inputDirectory, languageParsers, null, printTrees)
        }
    }

    private fun processProject(
        projectFile: File,
        languageParsers: Map<Language, Parser>,
        holdout: Dataset?,
        printTrees: Boolean
    ) {
        // TODO: log why we can't process the project
        val project = openProject(projectFile) ?: return
        languageParsers.forEach { (language, parser) ->
            val files = getAllFilesByLanguage(project, language)
            println("Found ${files.size} $language files")
            val labeledTrees = parser.parseFiles(files, project).flatMap { psiTreeRoot ->
                psiTreeRoot.splitPsiByGranularity(problem.granularityLevel)
                    .filter { root -> filters.all { it.isGoodTree(root) } }
                    .mapNotNull { problem.processTree(it) }
            }
            labeledTrees.forEach {
                storage.store(it, holdout, language)
                if (printTrees) it.root.printTree()
            }
        }
    }
}
