package storage.tree

import PATH_KEY
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiElement
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import labelextractor.LabeledTree
import psi.nodeProperties.nodeType
import psi.nodeProperties.token
import psi.preOrder
import psi.transformations.typeresolve.resolvedTokenType

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

fun convert(labeledTree: LabeledTree, withPaths: Boolean): String {
    val nodeRepresentations = collectNodeRepresentation(labeledTree.root)
    ProjectManager.getInstance()
    val path = if (withPaths) labeledTree.root.getUserData(PATH_KEY) else null
    val treeRepresentation = TreeRepresentation(labeledTree.label, path, nodeRepresentations)
    return jsonSerializer.encodeToString(treeRepresentation)
}
