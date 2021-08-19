package storage.tree

import Dataset
import PATH_KEY
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import labelextractor.LabeledTree
import org.jetbrains.kotlin.idea.util.getSourceRoot
import psi.nodeProperties.nodeType
import psi.nodeProperties.token
import psi.preOrder
import psi.transformations.typeresolve.resolvedTokenType
import storage.Storage
import java.io.File

/**
 * Store each tree in JSONL format (one sample per line)
 * Format description: https://jsonlines.org
 * Tree saves in Python150K format: https://www.sri.inf.ethz.ch/py150
 */
class JsonTreeStorage(outputDirectory: File, private val withPaths: Boolean) : Storage(outputDirectory) {

    override val fileExtension: String = "jsonl"
    private val jsonSerializer = Json { encodeDefaults = false }

    @Serializable
    private data class NodeRepresentation(
        val token: String?,
        val nodeType: String,
        val tokenType: String? = null,
        val children: List<Int>
    )
    @Serializable
    private data class TreeRepresentation(
        val label: String,
        val path: String? = null,
        val tree: List<NodeRepresentation>
    )

    private fun collectNodeRepresentation(root: PsiElement): List<NodeRepresentation> {
        val nodeToId = hashMapOf<PsiElement, Int>()
        root.preOrder().forEach {
            nodeToId[it] = nodeToId.size
        }
        return nodeToId.entries.sortedBy { it.value }.map { it.key }.map {
            val childrenIds = it.children.mapNotNull { child -> nodeToId[child] }
            NodeRepresentation(it.token, it.nodeType, it.resolvedTokenType, childrenIds)
        }
    }

    override fun convert(labeledTree: LabeledTree, holdout: Dataset?): String {
        val nodeRepresentations = collectNodeRepresentation(labeledTree.root)
        ProjectManager.getInstance()
        val path = if (withPaths) labeledTree.root.getUserData(PATH_KEY) else null
        val treeRepresentation = TreeRepresentation(labeledTree.label, path, nodeRepresentations)
        return jsonSerializer.encodeToString(treeRepresentation)
    }
}
