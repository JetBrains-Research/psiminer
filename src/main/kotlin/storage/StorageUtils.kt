package storage

import Dataset
import astminer.common.getNormalizedToken
import astminer.common.model.ASTPath
import astminer.common.model.Direction
import astminer.common.model.OrientedNodeType
import astminer.common.model.PathContext

data class XPathContext(val startTokenType: String, val pathContext: PathContext, val endTokenType: String) {
    companion object {
        fun createFromASTPath(path: ASTPath): XPathContext {
            val startTokenType =
                path.upwardNodes.first().getMetadata(TypeConstants.PSI_TYPE_METADATA_KEY)?.toString() ?: TypeConstants.NO_TYPE

            val startToken = path.upwardNodes.first().getNormalizedToken()
            val astNodes = path.upwardNodes.map { OrientedNodeType(it.getTypeLabel(), Direction.UP) } +
                    path.downwardNodes.map { OrientedNodeType(it.getTypeLabel(), Direction.DOWN) }
            val endToken = path.downwardNodes.last().getNormalizedToken()
            val pathContext = PathContext(startToken, astNodes, endToken)

            val endTokenType =
                path.downwardNodes.last().getMetadata(TypeConstants.PSI_TYPE_METADATA_KEY)?.toString() ?: TypeConstants.NO_TYPE

            return XPathContext(startTokenType, pathContext, endTokenType)
        }
    }
}

data class XPathContextId(
    val startTokenTypeId: Long,
    val startTokenId: Long,
    val pathId: Long,
    val endTokenId: Long,
    val endTokenTypeId: Long
)

data class XLabeledPathContexts<T>(val label: T, val xPathContexts: Collection<XPathContext>)

data class XLabeledPathContextIds<T>(val label: T, val xPathContexts: Collection<XPathContextId>)

interface XPathContextsStorage<LabelType> {
    val directoryPath: String
    val noTypes: Boolean
    fun store(xLabeledPathContexts: XLabeledPathContexts<LabelType>, dataset: Dataset)
    fun close()
}
