package storage.paths

import Dataset
import astminer.common.model.ASTPath
import astminer.common.storage.RankedIncrementalIdStorage
import astminer.common.storage.dumpIdStorageToCsv
import astminer.paths.PathMiner
import astminer.paths.PathRetrievalSettings
import com.intellij.psi.PsiElement
import problem.LabeledTree
import psi.nodeProperties.nodeType
import psi.nodeProperties.token
import storage.Storage
import java.io.File

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
    outputDirectory: File,
    private val pathWidth: Int,
    private val pathLength: Int,
    private val maxPathsInTrain: Int? = null,
    private val maxPathsInTest: Int? = null,
    private val nodesToNumbers: Boolean = false
) : Storage(outputDirectory) {

    override val fileExtension: String = "c2s"

    private val miner = PathMiner(PathRetrievalSettings(pathLength, pathWidth))
    private val nodeTypesIdStorage = RankedIncrementalIdStorage<String>()

    private data class HoldoutStatistic(var nSamples: Int = 0, var nPaths: Int = 0) {
        override fun toString(): String =
            "#samples: $nSamples, #paths: $nPaths, #rate: ${nPaths.toDouble() / nSamples}"
    }
    private val datasetStatistic = mutableMapOf<OutputDirection, HoldoutStatistic>()

    private fun unwrapAstPath(wrappedPath: ASTPath): List<PsiElement> =
        wrappedPath.upwardNodes.map { (it as AstminerNodeWrapper).psiNode } +
                (wrappedPath.topNode as AstminerNodeWrapper).psiNode +
                wrappedPath.downwardNodes.map { (it as AstminerNodeWrapper).psiNode }

    private fun nodeToString(node: PsiElement): String =
        if (nodesToNumbers) nodeTypesIdStorage.record(node.nodeType).toString()
        else node.nodeType

    // TODO: normalize tokens
    private fun pathToString(path: List<PsiElement>): String = StringBuilder()
        .append("\"${path.first().token?.replace("\n", "\\n")}\"")
        .append(",")
        .append(path.joinToString("|") { nodeToString(it) })
        .append(",")
        .append("\"${path.last().token?.replace("\n", "\\n")}\"")
        .toString()

    override fun convert(labeledTree: LabeledTree, outputDirection: OutputDirection): String {
        val wrappedNode = AstminerNodeWrapper(labeledTree.root)
        val maxPaths = if (outputDirection.holdout == Dataset.Train) maxPathsInTrain else maxPathsInTest
        val paths = miner.retrievePaths(wrappedNode).shuffled().let { it.take(maxPaths ?: it.size) }
        val unwrappedPaths = paths.map { unwrapAstPath(it) }
        val pathRepresentation = unwrappedPaths.joinToString(" ") { pathToString(it) }

        datasetStatistic.getOrPut(outputDirection) { HoldoutStatistic(0, 0) }.apply {
            nSamples += 1
            nPaths += paths.size
        }
        return "${labeledTree.label} $pathRepresentation"
    }

    override fun printStatistic() = datasetStatistic.forEach { println(it.value) }

    override fun close() {
        super.close()
        if (nodesToNumbers) dumpIdStorageToCsv(
            nodeTypesIdStorage,
            "nodeType",
            { it },
            outputDirectory.resolve("nodes_vocabulary.csv")
        )
    }
}
