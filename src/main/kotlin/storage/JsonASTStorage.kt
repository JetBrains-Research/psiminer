package storage

import Config
import Dataset
import psi.PsiNode
import java.io.File
import java.io.PrintWriter

/***
 * Store each tree in JSONL format (one sample per line)
 * Format description: https://jsonlines.org
 * Tree saves in Python150K format: https://www.sri.inf.ethz.ch/py150
 ***/
class JsonASTStorage(
    override val outputDirectory: File,
    override val config: Config,
) : Storage {

    private val datasetFileWriters = mutableMapOf<Dataset, PrintWriter>()
    private val datasetStatistic = mutableMapOf<Dataset, Int>()

    init {
        outputDirectory.mkdirs()
        val datasetName = outputDirectory.nameWithoutExtension
        Dataset.values().forEach {
            val holdoutFile = outputDirectory.resolve("$datasetName.${it.folderName}.jsonl")
            holdoutFile.createNewFile()
            datasetFileWriters[it] = PrintWriter(holdoutFile)
            datasetStatistic[it] = 0
        }
    }

    private data class NumeratedNode(val node: PsiNode, val id: Int, val children: List<NumeratedNode>)
    private data class DFSReturn(val numerateNode: NumeratedNode, val subtreeSize: Int)

    private fun dfsEnumerateTree(node: PsiNode, currentId: Int): DFSReturn {
        var step = 0
        val children = node.getChildren().map { child ->
            val dfsReturn = dfsEnumerateTree(child, currentId + 1 + step)
            step += dfsReturn.subtreeSize
            dfsReturn.numerateNode
        }
        return DFSReturn(NumeratedNode(node, currentId, children), step + 1)
    }

    private fun dfsOrder(node: NumeratedNode): List<NumeratedNode> {
        val order = mutableListOf(node)
        node.children.forEach {
            order.addAll(dfsOrder(it))
        }
        return order
    }

    private fun nodeToString(node: PsiNode, childrenIds: List<Int>): String =
        StringBuilder("{")
            .append("\"node\":\"${node.getTypeLabel()}\",")
            .append(if (config.resolveTypes) "\"type\":\"${node.resolvedTokenType}\"," else "")
            .append(if (childrenIds.isNotEmpty()) "\"children\":[${childrenIds.joinToString(",")}]," else "")
            .append("\"token\":\"${node.getNormalizedToken()}\"")
            .append("}")
            .toString()

    override fun store(sample: PsiNode, label: String, holdout: Dataset) {
        datasetStatistic[holdout] = datasetStatistic[holdout]?.plus(1) ?: 0
        val enumeratedTree = dfsEnumerateTree(sample, 0).numerateNode
        val stringTree = dfsOrder(enumeratedTree).map { nodeToString(it.node, it.children.map { c -> c.id }) }
        datasetFileWriters[holdout]?.println(
            "{\"label\":\"$label\",\"AST\":[${stringTree.joinToString(",")}]}"
        )
    }

    override fun printStatistic() =
        Dataset.values().forEach { println("${datasetStatistic[it]} samples in $it holdout") }

    override fun close() = datasetFileWriters.forEach { it.value.close() }

    companion object {
        const val name = "json"
    }
}
