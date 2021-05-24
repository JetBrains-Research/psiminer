package storage

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import problem.LabeledTree
import psi.nodeProperties.isHidden
import psi.nodeProperties.nodeType
import psi.nodeProperties.token
import java.io.File

/***
 * Store each tree in JSONL format (one sample per line)
 * Format description: https://jsonlines.org
 * Tree saves in Python150K format: https://www.sri.inf.ethz.ch/py150
 ***/
class JsonASTStorage(outputDirectory: File) : Storage(outputDirectory) {

    override val fileExtension: String = "jsonl"

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

    override fun convert(labeledTree: LabeledTree, outputDirection: OutputDirection): String {
        val nodesRepresentation = collectNodeRepresentation(labeledTree.root)
        val treeRepresentation = TreeRepresentation(labeledTree.label, nodesRepresentation)
        return Json.encodeToString(treeRepresentation)
    }
}
