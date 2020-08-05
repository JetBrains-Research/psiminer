package storage

class XCode2VecPathStorage(directoryPath: String) : XCountingPathStorage<String>(directoryPath) {
    override fun xPathContextIdsToString(xPathContextIds: List<XPathContextId>, label: String): String {
        val joinedPathContexts = xPathContextIds.joinToString(" ") { xPathContextId ->
            "${xPathContextId.startTokenTypeId},${xPathContextId.startTokenId},${xPathContextId.pathId}," +
                    "${xPathContextId.endTokenId},${xPathContextId.endTokenTypeId}"
        }
        return "$label $joinedPathContexts"
    }
}
