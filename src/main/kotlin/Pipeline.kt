import filter.*
import problem.MethodNamePrediction
import problem.Problem
import psi.PsiProjectParser
import storage.Code2SeqStorage
import storage.Storage
import java.io.File

class Pipeline(private val outputDirectory: File, private val config: Config) {

    private fun getStorage(): Storage = when (config.format) {
        Code2SeqStorage.name -> Code2SeqStorage(outputDirectory, config)
        else -> throw IllegalArgumentException("Unknown storage")
    }

    private fun getProblem(storage: Storage): Problem = when (config.problem) {
        MethodNamePrediction.name -> MethodNamePrediction(storage)
        else -> throw IllegalArgumentException("Unknown problem")
    }

    private fun getFilters(): List<Filter> = config.filters.map {
        when (it) {
            ClassConstructorFilter.name -> ClassConstructorFilter()
            AbstractMethodFilter.name -> AbstractMethodFilter()
            OverrideMethodFilter.name -> OverrideMethodFilter()
            TreeSizeFilter.name -> TreeSizeFilter(config.minTreeSize, config.maxTreeSize)
            CodeLengthFilter.name -> CodeLengthFilter(config.minCodeLength, config.maxCodeLength)
            else -> throw java.lang.IllegalArgumentException("Unknown filter")
        }
    }

    private fun getPsiProjectParser(problem: Problem, filters: List<Filter>): PsiProjectParser =
        PsiProjectParser(problem.granularityLevel, config) { tree, holdout ->
            if (filters.all { it.isGoodTree(tree) }) problem.processTree(tree, holdout)
            printTree(tree, true)
        }

    fun extractDataFromDataset(datasetDirectory: File) {
        val storage = getStorage()
        val problem = getProblem(storage)
        val filters = getFilters()
        val projectParser = getPsiProjectParser(problem, filters)

        Dataset.values().forEach { holdout ->
            val holdoutFile = datasetDirectory.resolve(holdout.folderName)
            holdoutFile.walk().maxDepth(1).filter { it.name != holdout.folderName }.forEach { holdoutProject ->
                projectParser.extractPsiFromProject(holdoutProject.path, holdout)
            }
        }

        storage.printStatistic()
        storage.close()
    }
}
