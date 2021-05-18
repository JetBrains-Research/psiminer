package storage.paths

import Dataset
import astminer.common.storage.RankedIncrementalIdStorage
import astminer.common.storage.dumpIdStorageToCsv
import astminer.paths.PathMiner
import astminer.paths.PathRetrievalSettings
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import psi.PsiNode
import storage.Storage
import storage.StorageConfig
import java.io.File
import java.io.PrintWriter

@Serializable
@SerialName("Code2seq")
class Code2SeqStorageConfig(
    val pathWidth: Int,                 // Maximum distance between two children of path LCA node
    val pathLength: Int,                // Maximum length of path
    val maxPathsInTrain: Int? = null,   // If not null then use only this number of paths to represent train trees
    val maxPathsInTest: Int? = null,    // If not null then use only this number of paths to represent val or test trees
    val nodesToNumbers: Boolean = false // If true then each node type is replaced with number
) : StorageConfig() {
    override fun createStorage(outputDirectory: File): Storage = Code2SeqStorage(this, outputDirectory)
}

/***
 * Use path-based representation to represent each tree
 * More details about format: https://github.com/tech-srl/code2seq
 ***/
class Code2SeqStorage(override val config: Code2SeqStorageConfig, override val outputDirectory: File) : Storage {

    private val datasetFileWriters = mutableMapOf<Dataset, PrintWriter>()
    private val nodesMap = RankedIncrementalIdStorage<String>()
    private val miner: PathMiner = PathMiner(PathRetrievalSettings(config.pathLength, config.pathWidth))

    private data class HoldoutStatistic(var nSamples: Int = 0, var nPaths: Int = 0) {
        override fun toString(): String =
            "#samples: $nSamples, #paths: $nPaths, #rate: ${nPaths.toDouble() / nSamples}"
    }

    private val datasetStatistic = Dataset.values().associate { it to HoldoutStatistic() }

    init {
        outputDirectory.mkdirs()
        val datasetName = outputDirectory.nameWithoutExtension
        Dataset.values().forEach {
            val holdoutFile = outputDirectory.resolve("$datasetName.${it.folderName}.c2s")
            holdoutFile.createNewFile()
            datasetFileWriters[it] = PrintWriter(holdoutFile)
        }
    }

    private fun nodePathToIds(pathNodes: List<String>): List<Long> = pathNodes.map { nodesMap.record(it) }

    private fun extractPathContexts(root: PsiNode, holdout: Dataset): List<PathContext> {
        val nPaths = if (holdout == Dataset.Train) config.maxPathsInTrain else config.maxPathsInTest
        return miner.retrievePaths(root).shuffled()
            .map { PathContext.createFromASTPath(it) }
            .let { it.take(nPaths ?: it.size) }
    }

    override fun store(sample: PsiNode, label: String, holdout: Dataset) {
        val pathContexts = extractPathContexts(sample, holdout)
        if (pathContexts.isEmpty()) return
        val stringPathContexts = pathContexts.joinToString(" ") {
            val nodePath = if (config.nodesToNumbers) nodePathToIds(it.nodePath) else it.nodePath
            val basePathContext = "${it.startToken},${nodePath.joinToString("|")},${it.endToken}"
            basePathContext
        }
        datasetStatistic[holdout]?.apply {
            nSamples += 1
            nPaths += pathContexts.size
        }
        datasetFileWriters[holdout]?.println("$label $stringPathContexts")
    }

    override fun printStatistic() = Dataset.values().forEach { println("$it statistic: ${datasetStatistic[it]}") }

    override fun close() {
        datasetFileWriters.forEach { it.value.close() }
        if (config.nodesToNumbers) dumpIdStorageToCsv(
            nodesMap,
            "node",
            { it },
            outputDirectory.resolve("nodes_vocabulary.csv")
        )
    }

    companion object {
        const val name: String = "code2seq"
    }
}
