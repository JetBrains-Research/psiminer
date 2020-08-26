package storage

class XCode2VecPathStorage(
        directoryPath: String, override val noTypes: Boolean
) : XCountingPathStorage<String>(directoryPath) {
    override fun xPathContextIdsToString(xPathContextIds: List<XPathContextId>, label: String): String {
        val joinedPathContexts = xPathContextIds.joinToString(" ") { xPathContextId ->
            val pathContext = "${xPathContextId.startTokenId},${xPathContextId.pathId},${xPathContextId.endTokenId}"
            if (noTypes) {
                return@joinToString pathContext
            }
            "${xPathContextId.startTokenTypeId},${pathContext},${xPathContextId.endTokenTypeId}"
        }
        return "$label $joinedPathContexts"
    }
}
