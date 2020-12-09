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

    private fun getPsiProjectParser(problem: Problem): PsiProjectParser =
        PsiProjectParser(problem.granularityLevel, config) { tree, holdout ->
            problem.processTree(tree, holdout)
//            printTree(tree, true)
        }

    fun extractDataFromDataset(datasetDirectory: File) {
        val storage = getStorage()
        val problem = getProblem(storage)
        val projectParser = getPsiProjectParser(problem)

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
