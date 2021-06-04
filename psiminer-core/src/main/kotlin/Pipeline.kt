import psi.Parser
import psi.printTree
import java.io.File

class Pipeline(private val config: PipelineConfig) {

    private val languageParsers = config.languages.associateWith {
        Parser(it, config.nodeIgnoreRules, config.treeTransformations)
    }

    private fun checkFolderIsDataset(folder: File): Boolean {
        val folderDirNames = folder.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: return false
        return Dataset.values().all { folderDirNames.contains(it.folderName) }
    }

    fun extract(inputDirectory: File) {
        println("Starting data extraction using the following parser configurations")
        languageParsers.forEach { println(it.value) }
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
                    processProject(holdoutProjectFile, holdout)
                }
            }
        } else {
            println("No dataset found. Process all sources under passed path")
            processProject(inputDirectory, null)
        }
    }

    private fun processProject(projectFile: File, holdout: Dataset?) {
        // TODO: log why we can't process the project
        val project = openProject(projectFile) ?: return
        languageParsers.forEach { (language, parser) ->
            println("Working on $language language")
            var processedDataPoints = 0
            parser.parseProject(
                project,
                config.labelExtractor.granularityLevel,
                handlePsiFile = { psiRoot ->
                    if (config.filters.all { it.validateTree(psiRoot, language) }) {
                        config.labelExtractor.extractLabel(psiRoot, language)
                    } else null
                },
                outputCallback = {
                    config.storage.store(it, holdout, language)
                    processedDataPoints += 1
                    if (processedDataPoints % 10000 == 0) println("Processed $processedDataPoints data points")
                    if (config.parameters.printTrees) it.root.printTree()
                }
            )
        }
//        closeProject(project)
    }
}
