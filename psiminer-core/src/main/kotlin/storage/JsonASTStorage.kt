package storage

import Dataset
import Language
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.rd.util.getOrCreate
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import problem.LabeledTree
import psi.nodeProperties.isHidden
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

    private data class OutputDirection(val holdout: Dataset, val language: Language)
    private val datasetFileWriters = mutableMapOf<OutputDirection, PrintWriter>()
    private val datasetStatistic = mutableMapOf<OutputDirection, Int>()

    init {
        outputDirectory.mkdirs()
    }

    @Serializable
    private data class NodeRepresentation(
        @Transient val id: Int? = null,
        val token: String?,
        val nodeType: String,
        val children: List<Int>,
    )
    @Serializable
    private data class TreeRepresentation(val label: String, val nodes: List<NodeRepresentation>)

    private class NumerateTreeVisitor: PsiRecursiveElementVisitor() {
        val nodeToId = hashMapOf<PsiElement, Int>()

        override fun visitElement(element: PsiElement) {
            if (!element.isHidden) nodeToId[element] = nodeToId.size
            super.visitElement(element) // super call in the end of recursion method correspond to preorder traverse
        }
    }

    private fun collectNodeRepresentation(root: PsiElement): List<NodeRepresentation> {
        val numerateTreeVisitor = NumerateTreeVisitor()
        root.accept(numerateTreeVisitor)
        return numerateTreeVisitor.nodeToId
            .map { (node, id) ->
                val childrenIds = node.children.mapNotNull { numerateTreeVisitor.nodeToId[it] }
                NodeRepresentation(id, node.token, node.nodeType, childrenIds)
            }
            .sortedBy { it.id }
    }

    override fun store(labeledTree: LabeledTree, holdout: Dataset, language: Language) {
        val outputDirection = OutputDirection(holdout, language)

        val nodesRepresentation = collectNodeRepresentation(labeledTree.root)
        val treeRepresentation = TreeRepresentation(labeledTree.label, nodesRepresentation)

        datasetStatistic[outputDirection] = datasetStatistic.getOrCreate(outputDirection) { 0 }.plus(1)
        datasetFileWriters.getOrPut(outputDirection) {
            val outputFile = outputDirectory
                .resolve(language.name)
                .resolve("${holdout.folderName}.$jsonlExtension")
            outputFile.parentFile.mkdirs()
            outputFile.createNewFile()
            PrintWriter(outputFile)
        }.println(Json.encodeToString(treeRepresentation))
    }

    override fun printStatistic() =
        datasetStatistic.forEach { println("${it.value} samples for ${it.key.language} in ${it.key.holdout} holdout") }

    override fun close() = datasetFileWriters.forEach { it.value.close() }

    companion object {
        const val jsonlExtension = "jsonl"
    }
}
