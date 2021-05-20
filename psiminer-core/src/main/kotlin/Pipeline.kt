import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.ex.ProjectManagerEx
import filter.Filter
import problem.Problem
import psi.nodeIgnoreRules.PsiNodeIgnoreRule
import psi.parser.ParserFactory
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
        val parserFactory = ParserFactory(ignoreNodeRule)
        val isDataset = checkFolderIsDataset(inputDirectory)

        if (isDataset) {
            println("Dataset structure is detected.")
            Dataset.values().forEach { holdout ->
                val holdoutFolder = inputDirectory.resolve(holdout.folderName)
                val holdoutProjects = holdoutFolder
                    .walk().maxDepth(1).toList().filter { it.name != holdout.folderName && !it.isFile }
                holdoutProjects.forEachIndexed { index, holdoutProject ->
                    println("Start parsing $holdout.${holdoutProject.name} project " +
                            "(${index + 1}/${holdoutProjects.size})")
                    extractFromProject(holdoutProject, holdout, languages, parserFactory, printTrees)
                }
            }
        } else {
            println("No dataset found. Process all sources under passed path")
            // TODO: handle not dataset projects/files
//            extractFromProject(inputDirectory, null, languages, parserFactory, printTrees)
        }
    }

    private fun extractFromProject(
        projectFile: File,
        holdout: Dataset,
        languages: List<Language>,
        parserFactory: ParserFactory,
        printTrees: Boolean
    ) {
        // TODO: log error
        val project = ProjectUtil.openOrImport(projectFile.path, null, true) ?: return
        languages.forEach { language ->
            val parser = parserFactory.createParser(language)
            val psiTrees = parser.parseProject(project)
            val labeledResults = splitPsiByGranularity(psiTrees, problem.granularityLevel)
                .filter { root -> filters.all { it.isGoodTree(root) } }
                .mapNotNull { problem.processTree(it) }
            labeledResults.forEach {
                storage.store(it, holdout, language)
                if (printTrees) it.root.printTree()
            }
        }
        // Force close project to avoid making physical modification of data
        // due to corresponding PSI trees modifications
        ApplicationManager.getApplication().invokeAndWait {
            ProjectManagerEx.getInstanceEx().forceCloseProject(project)
        }
    }
}
