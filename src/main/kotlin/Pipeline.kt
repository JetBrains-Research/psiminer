import filter.*
import problem.MethodNamePrediction
import problem.LabelExtractor
import psi.PsiProjectParser
import storage.Code2SeqStorage
import storage.JsonASTStorage
import storage.Storage
import java.io.File

class Pipeline(private val outputDirectory: File, private val config: Config) {

    private fun getStorage(): Storage = when (config.format) {
        Code2SeqStorage.name -> Code2SeqStorage(outputDirectory, config)
        JsonASTStorage.name -> JsonASTStorage(outputDirectory, config)
        else -> throw IllegalArgumentException("Unknown storage ${config.format}")
    }

    private fun getLabelExtractor(): LabelExtractor = when (config.problem) {
        MethodNamePrediction.name -> MethodNamePrediction()
        else -> throw IllegalArgumentException("Unknown problem ${config.problem}")
    }

    private fun getFilters(): List<Filter> = config.filters.map {
        when (it) {
            ClassConstructorFilter.name -> ClassConstructorFilter()
            AbstractMethodFilter.name -> AbstractMethodFilter()
            OverrideMethodFilter.name -> OverrideMethodFilter()
            TreeSizeFilter.name -> TreeSizeFilter(config.minTreeSize, config.maxTreeSize)
            CodeLengthFilter.name -> CodeLengthFilter(config.minCodeLength, config.maxCodeLength)
            EmptyMethodFilter.name -> EmptyMethodFilter()
            else -> throw java.lang.IllegalArgumentException("Unknown filter $it")
        }
    }

    fun extractDataFromDataset(datasetDirectory: File) {
        val storage = getStorage()
        val problem = getLabelExtractor()
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
