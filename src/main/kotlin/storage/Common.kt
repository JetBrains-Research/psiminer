package storage

import Dataset
import astminer.common.model.PathContext


data class XPathContext(val startTokenType: String, val pathContext: PathContext, val endTokenType: String)
data class XPathContextId(
    val startTokenTypeId: Long,
    val startTokenId: Long,
    val pathId: Long,
    val endTokenId: Long,
    val endTokenTypeId: Long
)

data class XLabeledPathContexts<T>(val label: T, val xPathContexts: Collection<XPathContext>)
data class XLabeledPathContextIds<T>(val label: T, val xPathContexts: Collection<XPathContextId>)

val tokenTypeToCsvString: (String) -> String = { tokenType -> tokenType }

interface XPathContextsStorage<LabelType> {
    val directoryPath: String
    fun store(xLabeledPathContexts: XLabeledPathContexts<LabelType>, dataset: Dataset)
    fun close()
}