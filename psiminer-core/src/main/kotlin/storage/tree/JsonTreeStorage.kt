package storage.tree

import com.intellij.psi.PsiElement
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import labelextractor.LabeledTree
import psi.nodeProperties.nodeType
import psi.nodeProperties.token
import storage.Storage
import java.io.File

/***
 * Store each tree in JSONL format (one sample per line)
 * Format description: https://jsonlines.org
 * Tree saves in Python150K format: https://www.sri.inf.ethz.ch/py150
 ***/
class JsonTreeStorage(outputDirectory: File) : Storage(outputDirectory) {

    override val fileExtension: String = "jsonl"

    @Serializable
    private data class NodeRepresentation(
        val token: String?,
        val nodeType: String,
        val children: List<Int>,
    )
    @Serializable
    private data class TreeRepresentation(val label: String, val tree: List<NodeRepresentation>)

    private fun collectNodeRepresentation(root: PsiElement): List<NodeRepresentation> {
        val numerateTreeVisitor = NumerateTreeVisitor()
        root.accept(numerateTreeVisitor)
        return numerateTreeVisitor.orderTree().map {
            val childrenIds = it.children.mapNotNull { child -> numerateTreeVisitor.nodeToId[child] }
            NodeRepresentation(it.token, it.nodeType, childrenIds)
        }
    }

    override fun convert(labeledTree: LabeledTree, outputDirection: OutputDirection): String {
        val nodesRepresentation = collectNodeRepresentation(labeledTree.root)
        val treeRepresentation = TreeRepresentation(labeledTree.label, nodesRepresentation)
        return Json.encodeToString(treeRepresentation)
    }
}
