import filter.Filter
import problem.Problem
import psi.PsiParserParameters
import psi.PsiProjectParser
import storage.Storage
import java.io.File

class Pipeline(private val filters: List<Filter>, private val problem: Problem, private val storage: Storage) {

    fun extractDataFromDataset(
        datasetDirectory: File,
        parserParameters: PsiParserParameters,
        printTrees: Boolean = false
    ) {
        val projectParser = PsiProjectParser(
            parserParameters,
            problem.granularityLevel,
            filters,
            problem::processTree,
            storage::store,
            printTrees
        )

        Dataset.values().forEach { holdout ->
            val holdoutFolder = datasetDirectory.resolve(holdout.folderName)
            val holdoutProjects = holdoutFolder
                .walk().maxDepth(1).toList()
                .filter { it.name != holdout.folderName && !it.isFile }
            holdoutProjects.forEachIndexed { index, holdoutProject ->
                println("Start parsing $holdout.${holdoutProject.name} project (${index + 1}/${holdoutProjects.size})")
                projectParser.extractPsiFromProject(holdoutProject.path, holdout)
            }
        }

        storage.printStatistic()
        storage.close()
    }
}
