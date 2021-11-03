package storage.tree

import PATH_KEY
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiElement
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import labelextractor.LabeledTree
import psi.nodeProperties.nodeType
import psi.nodeProperties.tokenV1
import psi.preOrder
import psi.transformations.typeresolve.resolvedTokenType

private val jsonSerializer = Json { encodeDefaults = false }

@Serializable
private data class NodeRepresentationV1(
    val node: String,
    val tokenType: String? = null,
    val children: List<Int>? = null,
    val token: String
)

@Serializable
private data class TreeRepresentationV1(
    val label: String,
    val path: String? = null,
    val AST: List<NodeRepresentationV1>
)

private fun collectNodeRepresentation(root: PsiElement): List<NodeRepresentationV1> {
    val nodeToId = hashMapOf<PsiElement, Int>()
    root.preOrder().forEach {
        nodeToId[it] = nodeToId.size
    }
    return nodeToId.entries.sortedBy { it.value }.map { it.key }.map {
        val childrenIds = it.children.mapNotNull { child -> nodeToId[child] }
        NodeRepresentationV1(
            it.nodeType,
            it.resolvedTokenType,
            childrenIds.takeIf { childrenIds.isNotEmpty() },
            it.tokenV1
        )
    }
}

fun convertV1(labeledTree: LabeledTree, withPaths: Boolean): String {
    val nodeRepresentations = collectNodeRepresentation(labeledTree.root)
    ProjectManager.getInstance()
    val path = if (withPaths) labeledTree.root.getUserData(PATH_KEY) else null
    val treeRepresentation = TreeRepresentationV1(labeledTree.label, path, nodeRepresentations)
    return jsonSerializer.encodeToString(treeRepresentation)
}
