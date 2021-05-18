package storage

import Config
import Dataset
import astminer.common.storage.RankedIncrementalIdStorage
import astminer.common.storage.dumpIdStorageToCsv
import astminer.paths.PathMiner
import astminer.paths.PathRetrievalSettings
import psi.PsiNode
import java.io.File
import java.io.PrintWriter

/***
 * Use path-based representation of sample
 * More details about this format: https://github.com/tech-srl/code2seq
 ***/
class Code2SeqStorage(
    override val outputDirectory: File,
    override val config: Config,
) : Storage {

    private val datasetFileWriters = mutableMapOf<Dataset, PrintWriter>()
    private val nodesMap = RankedIncrementalIdStorage<String>()
    private val miner: PathMiner

    private data class HoldoutStatistic(var nSamples: Int = 0, var nPaths: Int = 0) {
        override fun toString(): String =
            "#samples: $nSamples, #paths: $nPaths, #rate: ${nPaths.toDouble() / nSamples}"
    }
    private val datasetStatistic = Dataset.values().map { it to HoldoutStatistic() }.toMap()

    init {
        if (config.maxPathLength == null || config.maxPathWidth == null) throw IllegalArgumentException(
                "Found null value for max path length or max path width, specify it to use path based representation"
            )
        miner = PathMiner(PathRetrievalSettings(config.maxPathLength, config.maxPathWidth))
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
            .let { if (config.resolvedTypesFirst) PathContext.groupByResolvedTypes(it) else it }
            .let { it.take(nPaths ?: it.size) }
    }

    override fun store(sample: PsiNode, label: String, holdout: Dataset) {
        val pathContexts = extractPathContexts(sample, holdout)
        if (pathContexts.isEmpty()) return
        val stringPathContexts = pathContexts.joinToString(" ") {
            val nodePath = if (config.nodesToNumbers) nodePathToIds(it.nodePath) else it.nodePath
            val basePathContext = "${it.startToken},${nodePath.joinToString("|")},${it.endToken}"
            if (config.resolveTypes) "${it.startType},$basePathContext,${it.endType}" else basePathContext
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
