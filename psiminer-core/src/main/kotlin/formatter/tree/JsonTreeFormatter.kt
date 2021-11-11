package formatter.tree

import PATH_KEY
import com.intellij.psi.PsiElement
import formatter.Formatter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import labelextractor.LabeledTree
import psi.NodeRange
import psi.nodeProperties.nodeType
import psi.nodeProperties.token
import psi.nodeProperties.tokenOldFormat
import psi.nodeRange
import psi.preOrder
import psi.transformations.typeresolve.resolvedTokenType

/**
 * Format description: https://jsonlines.org
 * Tree converts to Python150K format: https://www.sri.inf.ethz.ch/py150
 */
class JsonTreeFormatter(
    private val withPaths: Boolean,
    private val withRanges: Boolean,
    private val useOldTokenFormat: Boolean = false
) : Formatter {
    private val jsonSerializer = Json { encodeDefaults = false }

    @Serializable
    data class NodeRepresentation(
        val token: String?,
        val nodeType: String,
        val tokenType: String? = null,
        val range: NodeRange? = null,
        val children: List<Int>
    )

    @Serializable
    data class TreeRepresentation(
        val label: String,
        val path: String? = null,
        val tree: List<NodeRepresentation>
    )

    fun collectNodeRepresentation(root: PsiElement): List<NodeRepresentation> {
        val nodeToId = hashMapOf<PsiElement, Int>()
        root.preOrder().forEach {
            nodeToId[it] = nodeToId.size
        }
        return nodeToId.entries.sortedBy { it.value }.map { it.key }.map {
            val childrenIds = it.children.mapNotNull { child -> nodeToId[child] }
            val token = if (useOldTokenFormat) it.tokenOldFormat else it.token
            NodeRepresentation(
                token,
                it.nodeType,
                it.resolvedTokenType,
                if (withRanges) it.nodeRange() else null,
                childrenIds
            )
        }
    }

    fun collectTreeRepresentation(labeledTree: LabeledTree): TreeRepresentation {
        val nodeRepresentations = collectNodeRepresentation(labeledTree.root)
        val path = if (withPaths) labeledTree.root.getUserData(PATH_KEY) else null
        return TreeRepresentation(labeledTree.label, path, nodeRepresentations)
    }

    override fun format(labeledTree: LabeledTree): String {
        val treeRepresentation = collectTreeRepresentation(labeledTree)
        return jsonSerializer.encodeToString(treeRepresentation)
    }
}
