package storage.paths

import Dataset
import Language
import astminer.common.storage.RankedIncrementalIdStorage
import astminer.common.storage.dumpIdStorageToCsv
import astminer.paths.PathMiner
import astminer.paths.PathRetrievalSettings
import com.intellij.psi.PsiElement
import problem.LabeledTree
import storage.Storage
import java.io.File
import java.io.PrintWriter

/***
 * Use path-based representation to represent each tree
 * More details about format: https://github.com/tech-srl/code2seq
 * @param pathWidth: Maximum distance between two children of path LCA node
 * @param pathLength: Maximum length of path
 * @param maxPathsInTrain: If not null then use only this number of paths to represent train trees
 * @param maxPathsInTest: If not null then use only this number of paths to represent val or test trees
 * @param nodesToNumbers: If true then each node type is replaced with number
 ***/
class Code2SeqStorage(
    override val outputDirectory: File,
    private val pathWidth: Int,
    private val pathLength: Int,
    private val maxPathsInTrain: Int? = null,
    private val maxPathsInTest: Int? = null,
    private val nodesToNumbers: Boolean = false
) : Storage {

    private val datasetFileWriters = mutableMapOf<Dataset, PrintWriter>()
    private val nodesMap = RankedIncrementalIdStorage<String>()
    private val miner: PathMiner = PathMiner(PathRetrievalSettings(pathLength, pathWidth))

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

    private fun extractPathContexts(root: PsiElement, holdout: Dataset): List<PathContext> {
        val nPaths = if (holdout == Dataset.Train) maxPathsInTrain else maxPathsInTest
        // TODO: fix path-based storage. Implement PsiElement wrapper for path miner.
        return emptyList()
//        return miner.retrievePaths(root).shuffled()
//            .map { PathContext.createFromASTPath(it) }
//            .let { it.take(nPaths ?: it.size) }
    }

    override fun store(labeledTree: LabeledTree, holdout: Dataset, language: Language) {
        val pathContexts = extractPathContexts(labeledTree.root, holdout)
        if (pathContexts.isEmpty()) return
        val stringPathContexts = pathContexts.joinToString(" ") {
            val nodePath = if (nodesToNumbers) nodePathToIds(it.nodePath) else it.nodePath
            val basePathContext = "${it.startToken},${nodePath.joinToString("|")},${it.endToken}"
            basePathContext
        }
        datasetStatistic[holdout]?.apply {
            nSamples += 1
            nPaths += pathContexts.size
        }
        datasetFileWriters[holdout]?.println("${labeledTree.label} $stringPathContexts")
    }

    override fun printStatistic() = Dataset.values().forEach { println("$it statistic: ${datasetStatistic[it]}") }

    override fun close() {
        datasetFileWriters.forEach { it.value.close() }
        if (nodesToNumbers) dumpIdStorageToCsv(
                nodesMap,
                "node",
                { it },
                outputDirectory.resolve("nodes_vocabulary.csv")
            )
    }
}
