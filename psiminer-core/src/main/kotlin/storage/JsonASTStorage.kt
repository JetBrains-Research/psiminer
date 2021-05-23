package storage

import Dataset
import Language
import com.intellij.psi.PsiElement
import problem.LabeledTree
import psi.nodeProperties.nodeType
import psi.nodeProperties.token
import java.io.File
import java.io.PrintWriter

/***
 * Store each tree in JSONL format (one sample per line)
 * Format description: https://jsonlines.org
 * Tree saves in Python150K format: https://www.sri.inf.ethz.ch/py150
 ***/
class JsonASTStorage(override val outputDirectory: File) : Storage {

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

    private data class NumeratedNode(val node: PsiElement, val id: Int, val children: List<NumeratedNode>)
    private data class DFSReturn(val numerateNode: NumeratedNode, val subtreeSize: Int)

    private fun dfsEnumerateTree(node: PsiElement, currentId: Int): DFSReturn {
        var step = 0
        val children = node.children.map { child ->
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

    private fun nodeToString(node: PsiElement, childrenIds: List<Int>): String =
        StringBuilder("{")
            .append("\"node\":\"${node.nodeType}\",")
            .append(if (childrenIds.isNotEmpty()) "\"children\":[${childrenIds.joinToString(",")}]," else "")
            .append("\"token\":\"${node.token}\"")
            .append("}")
            .toString()

    override fun store(labeledTree: LabeledTree, holdout: Dataset, language: Language) {
        datasetStatistic[holdout] = datasetStatistic[holdout]?.plus(1) ?: 0
        val enumeratedTree = dfsEnumerateTree(labeledTree.root, 0).numerateNode
        val stringTree = dfsOrder(enumeratedTree).map { nodeToString(it.node, it.children.map { c -> c.id }) }
        datasetFileWriters[holdout]?.println(
            "{\"label\":\"${labeledTree.label}\",\"AST\":[${stringTree.joinToString(",")}]}"
        )
    }

    override fun printStatistic() =
        Dataset.values().forEach { println("${datasetStatistic[it]} samples in $it holdout") }

    override fun close() = datasetFileWriters.forEach { it.value.close() }
}
