import filter.Filter
import problem.Problem
import psi.PsiProjectParser
import storage.Storage
import java.io.File

class Pipeline(private val config: Config) {

    private fun getStorage(outputDirectory: File): Storage = config.storage.createStorage(outputDirectory)

    private fun getProblem(): Problem = config.problem.createProblem()

    private fun getFilters(): List<Filter> = config.filters.map { it.createFilter() }

    fun extractDataFromDataset(datasetDirectory: File, outputDirectory: File) {
        val storage = getStorage(outputDirectory)
        val problem = getProblem()
        val filters = getFilters()
        val projectParser = PsiProjectParser(
            config,
            problem.granularityLevel,
            filters,
            problem::processTree,
            storage::store
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
