import astminer.common.model.*
import astminer.common.storage.*
import astminer.paths.CountingPathStorage
import java.io.File

const val DEFAULT_FRAGMENTS_PER_BATCH = 100L

data class XPathContext(val startTokenType: String, val pathContext: PathContext, val endTokenType: String)
data class XPathContextId(
    val startTokenTypeId: Long,
    val startTokenId: Long,
    val pathId: Long,
    val endTokenId: Long,
    val endTokenTypeId: Long
)

data class XLabeledPathContexts<T>(val label: T, val pathContexts: Collection<XPathContext>)
data class XLabeledPathContextIds<T>(val label: T, val pathContexts: Collection<XPathContextId>)


interface XPathStorage<LabelType> {
    fun store(labeledPathContexts: XLabeledPathContexts<LabelType>)
    fun save()
    fun save(pathsLimit: Long, tokensLimit: Long)
}

abstract class XCountingPathStorage<LabelType>(
    private val outputFolderPath: String,
    val batchMode: Boolean = true,
    val fragmentsPerBatch: Long = DEFAULT_FRAGMENTS_PER_BATCH) : XPathStorage<LabelType> {

    private var contextsFileIndex = 0
    private var currentFragmentsCount = 0

    protected val tokensTypesMap: RankedIncrementalIdStorage<String> = RankedIncrementalIdStorage()
    protected val tokensMap: RankedIncrementalIdStorage<String> = RankedIncrementalIdStorage()
    protected val orientedNodeTypesMap: RankedIncrementalIdStorage<OrientedNodeType> = RankedIncrementalIdStorage()
    protected val pathsMap: RankedIncrementalIdStorage<List<Long>> = RankedIncrementalIdStorage()

    protected val labeledPathContextIdsList: MutableList<XLabeledPathContextIds<LabelType>> = mutableListOf()

    private fun dumpTokenTypeStorage(file: File, tokensLimit: Long) {
        dumpIdStorageToCsv(tokensTypesMap, "token_type", tokenToCsvString, file, tokensLimit)
    }

    private fun dumpTokenStorage(file: File, tokensLimit: Long) {
        dumpIdStorageToCsv(tokensMap, "token", tokenToCsvString, file, tokensLimit)
    }

    private fun dumpOrientedNodeTypesStorage(file: File) {
        dumpIdStorageToCsv(orientedNodeTypesMap, "node_type", orientedNodeToCsvString, file, Long.MAX_VALUE)
    }

    private fun dumpPathsStorage(file: File, pathsLimit: Long) {
        dumpIdStorageToCsv(pathsMap, "path", pathToCsvString, file, pathsLimit)
    }

    abstract fun dumpPathContexts(file: File, tokensLimit: Long, pathsLimit: Long)

    private fun doStore(xpathContext: XPathContext): XPathContextId {
        val startTokenTypeId = tokensTypesMap.record(xpathContext.startTokenType)
        val startTokenId = tokensMap.record(xpathContext.pathContext.startToken)
        val endTokenId = tokensMap.record(xpathContext.pathContext.endToken)
        val endTokenTypeId = tokensTypesMap.record(xpathContext.endTokenType)
        val orientedNodesIds = xpathContext.pathContext.orientedNodeTypes.map { orientedNodeTypesMap.record(it) }
        val pathId = pathsMap.record(orientedNodesIds)
        return XPathContextId(startTokenTypeId, startTokenId, pathId, endTokenId, endTokenTypeId)
    }

    private fun dumpPathContextsIfNeeded() {
        if (!batchMode || currentFragmentsCount < fragmentsPerBatch) {
            return
        }
        File(outputFolderPath).mkdirs()
        dumpPathContexts(
            File("$outputFolderPath/path_contexts_${contextsFileIndex++}.csv"),
            Long.MAX_VALUE, Long.MAX_VALUE)
        labeledPathContextIdsList.clear()
        currentFragmentsCount = 0
    }

    override fun store(labeledPathContexts: XLabeledPathContexts<LabelType>) {
        val labeledPathContextIds = XLabeledPathContextIds(
            labeledPathContexts.label,
            labeledPathContexts.pathContexts.map { doStore(it) }
        )
        labeledPathContextIdsList.add(labeledPathContextIds)
        currentFragmentsCount++


        dumpPathContextsIfNeeded()
    }

    override fun save() {
        save(pathsLimit = Long.MAX_VALUE, tokensLimit = Long.MAX_VALUE)
    }

    override fun save(pathsLimit: Long, tokensLimit: Long) {
        if (batchMode && (pathsLimit < Long.MAX_VALUE || tokensLimit < Long.MAX_VALUE)) {
            println("Ignoring path and token limit settings due to batchMode processing")
        }
        File(outputFolderPath).mkdirs()
        dumpTokenTypeStorage(File("$outputFolderPath/tokens_types.csv"), tokensLimit)
        dumpTokenStorage(File("$outputFolderPath/tokens.csv"), tokensLimit)
        dumpOrientedNodeTypesStorage(File("$outputFolderPath/node_types.csv"))
        dumpPathsStorage(File("$outputFolderPath/paths.csv"), pathsLimit)

        if (!batchMode) {
            dumpPathContexts(File("$outputFolderPath/path_contexts.csv"), tokensLimit, pathsLimit)
        } else {
            dumpPathContexts(
                File("$outputFolderPath/path_contexts_${contextsFileIndex++}.csv"),
                Long.MAX_VALUE, Long.MAX_VALUE)
        }
    }
}


class XCode2VecPathStorage(outputFolderPath: String) : XCountingPathStorage<String>(outputFolderPath) {
    override fun dumpPathContexts(file: File, tokensLimit: Long, pathsLimit: Long) {
        val lines = mutableListOf<String>()
        labeledPathContextIdsList.forEach { labeledPathContextIds ->
            val pathContextIdsString = labeledPathContextIds.pathContexts.filter {
                tokensTypesMap.getIdRank(it.startTokenTypeId) <= tokensLimit &&
                tokensMap.getIdRank(it.startTokenId) <= tokensLimit &&
                        tokensMap.getIdRank(it.endTokenId) <= tokensLimit &&
                        tokensTypesMap.getIdRank(it.endTokenTypeId) <= tokensLimit &&
                        pathsMap.getIdRank(it.pathId) <= pathsLimit
            }.joinToString(separator = " ") { pathContextId ->
                "${pathContextId.startTokenTypeId},${pathContextId.startTokenId},${pathContextId.pathId},${pathContextId.endTokenId},${pathContextId.endTokenTypeId}"
            }
            lines.add("${labeledPathContextIds.label} $pathContextIdsString")
        }
        writeLinesToFile(lines, file)
    }
}