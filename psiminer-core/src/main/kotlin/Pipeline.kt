import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.serviceContainer.AlreadyDisposedException
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
        ignoreNodeRule: List<PsiNodeIgnoreRule>,
        printTrees: Boolean = false
    ) {
        val parser = Parser(ignoreNodeRule)
        val isDataset = checkFolderIsDataset(inputDirectory)

        if (isDataset) {
            println("Dataset structure is detected.")
            Dataset.values().forEach { holdout ->
                val holdoutFolder = inputDirectory.resolve(holdout.folderName)
                val holdoutProjects = holdoutFolder
                    .walk().maxDepth(1).toList().filter { it.name != holdout.folderName && !it.isFile }
                holdoutProjects.forEachIndexed { index, holdoutProject ->
                    println("Process $holdout.${holdoutProject.name} project (${index + 1}/${holdoutProjects.size})")
                    extractFromProject(parser, languages, holdoutProject, holdout, printTrees)
                }
            }
        } else {
            println("No dataset found. Process all sources under passed path")
            // TODO: handle not dataset projects/files
            // extractFromProject(inputDirectory, null, languages, parserFactory, printTrees)
        }
    }

    private fun extractFromProject(
        parser: Parser,
        languages: List<Language>,
        projectFile: File,
        holdout: Dataset,
        printTrees: Boolean
    ) {
        // TODO: log error
        val project = ProjectUtil.openOrImport(projectFile.path, null, true) ?: return
        languages.forEach { language ->
            val psiTrees = parser.parseProject(project, language)
            val labeledResults = splitPsiByGranularity(psiTrees, problem.granularityLevel)
                .filter { root -> filters.all { it.isGoodTree(root) } }
                .mapNotNull { problem.processTree(it) }
            labeledResults.forEach {
                // Some modifications during filtering or label extracting
                // may add new white spaces (e.g. after renaming method names)
                // Therefore manually check if we need to hide such nodes again
                if (parser.isWhiteSpaceHidden()) parser.hideAllWhiteSpaces(it.root)
            }
            labeledResults.forEach {
                storage.store(it, holdout, language)
                if (printTrees) it.root.printTree()
            }
        }
        // Force close project to avoid making physical modification of data
        // due to corresponding PSI trees modifications
        try {
            ProjectManagerEx.getInstanceEx().forceCloseProject(project)
        } catch (e: AlreadyDisposedException) {
            // TODO: figure out why this happened
            println(e.message)
        }
    }
}
