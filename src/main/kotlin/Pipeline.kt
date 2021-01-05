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

    private fun getProblem(): Problem = when (config.problem) {
        MethodNamePrediction.name -> MethodNamePrediction()
        else -> throw IllegalArgumentException("Unknown problem")
    }

    private fun getFilters(): List<Filter> = config.filters.map {
        when (it) {
            ClassConstructorFilter.name -> ClassConstructorFilter()
            AbstractMethodFilter.name -> AbstractMethodFilter()
            OverrideMethodFilter.name -> OverrideMethodFilter()
            TreeSizeFilter.name -> TreeSizeFilter(config.minTreeSize, config.maxTreeSize)
            CodeLengthFilter.name -> CodeLengthFilter(config.minCodeLength, config.maxCodeLength)
            EmptyMethodFilter.name -> EmptyMethodFilter()
            else -> throw java.lang.IllegalArgumentException("Unknown filter")
        }
    }

    fun extractDataFromDataset(datasetDirectory: File) {
        val storage = getStorage()
        val problem = getProblem()
        val filters = getFilters()
        val projectParser = PsiProjectParser(
            problem.granularityLevel, config, filters,
            problem::processTree, storage::store
        )

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
