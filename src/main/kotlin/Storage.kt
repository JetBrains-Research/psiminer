import astminer.common.model.*
import astminer.common.storage.*
import astminer.paths.CountingPathStorage
import com.intellij.util.containers.enumMapOf
import java.io.File
import java.util.*
import kotlin.collections.HashMap

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

enum class Dataset {
    Train,
    Test,
    Val
}

interface XPathStorage<LabelType> {
    fun store(labeledPathContexts: XLabeledPathContexts<LabelType>, dataset: Dataset)
    fun save()
    fun save(pathsLimit: Long, tokensLimit: Long)
}

abstract class XCountingPathStorage<LabelType>(
    private val outputFolderPath: String,
    val batchMode: Boolean = true,
    val fragmentsPerBatch: Long = DEFAULT_FRAGMENTS_PER_BATCH) : XPathStorage<LabelType> {

    private var contextsFileIndexMap = mapOf(
        Dataset.Train to 0,
        Dataset.Test to 0,
        Dataset.Val to 0
    )
    // private var currentFragmentsCount = 0

    protected val tokensTypesMap: RankedIncrementalIdStorage<String> = RankedIncrementalIdStorage()
    protected val tokensMap: RankedIncrementalIdStorage<String> = RankedIncrementalIdStorage()
    protected val orientedNodeTypesMap: RankedIncrementalIdStorage<OrientedNodeType> = RankedIncrementalIdStorage()
    protected val pathsMap: RankedIncrementalIdStorage<List<Long>> = RankedIncrementalIdStorage()

    protected val labeledPathContextIdsMap: Map<Dataset, MutableList<XLabeledPathContextIds<LabelType>>> =
        mapOf(
            Dataset.Train to mutableListOf(),
            Dataset.Test to mutableListOf(),
            Dataset.Val to mutableListOf()
        )

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

    abstract fun dumpPathContexts(file: File, tokensLimit: Long, pathsLimit: Long, labeledPathContextIdsList: MutableList<XLabeledPathContextIds<LabelType>>)

    private fun doStore(xpathContext: XPathContext): XPathContextId {
        val startTokenTypeId = tokensTypesMap.record(xpathContext.startTokenType)
        val startTokenId = tokensMap.record(xpathContext.pathContext.startToken)
        val endTokenId = tokensMap.record(xpathContext.pathContext.endToken)
        val endTokenTypeId = tokensTypesMap.record(xpathContext.endTokenType)
        val orientedNodesIds = xpathContext.pathContext.orientedNodeTypes.map { orientedNodeTypesMap.record(it) }
        val pathId = pathsMap.record(orientedNodesIds)
        return XPathContextId(startTokenTypeId, startTokenId, pathId, endTokenId, endTokenTypeId)
    }

    private fun dumpPathContextsIfNeeded(dataset: Dataset) {
        val labeledPathContextIdsList = labeledPathContextIdsMap[dataset] ?: error("unknown dataset type: $dataset")
        val contextsFileIndex = (contextsFileIndexMap[dataset] ?: error("unknown dataset type: $dataset")).inc()

        if (!batchMode || labeledPathContextIdsList.size < fragmentsPerBatch) {
            return
        }
        File(outputFolderPath).mkdirs()
        dumpPathContexts(
            File("$outputFolderPath/${dataset.name.toLowerCase()}_path_contexts_$contextsFileIndex.csv"),
            Long.MAX_VALUE, Long.MAX_VALUE,
            labeledPathContextIdsList
        )
        labeledPathContextIdsList.clear()
    }

    override fun store(labeledPathContexts: XLabeledPathContexts<LabelType>, dataset: Dataset) {
        val labeledPathContextIds = XLabeledPathContextIds(
            labeledPathContexts.label,
            labeledPathContexts.pathContexts.map { doStore(it) }
        )

        labeledPathContextIdsMap[dataset]?.add(labeledPathContextIds)

        dumpPathContextsIfNeeded(dataset)
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

        labeledPathContextIdsMap.forEach { dataset, labeledPathContextIdsList ->
            if (!batchMode) {
                dumpPathContexts(File("$outputFolderPath/path_contexts.csv"), tokensLimit, pathsLimit, labeledPathContextIdsList)
            } else {
                val contextsFileIndex = (contextsFileIndexMap[dataset] ?: error("unknown dataset type: $dataset")).inc()
                dumpPathContexts(
                    File("$outputFolderPath/path_contexts_${contextsFileIndex}.csv"),
                    Long.MAX_VALUE, Long.MAX_VALUE,
                    labeledPathContextIdsList
                )
            }
        }
    }
}


class XCode2VecPathStorage(outputFolderPath: String) : XCountingPathStorage<String>(outputFolderPath, batchMode = true) {
    override fun dumpPathContexts(file: File, tokensLimit: Long, pathsLimit: Long, labeledPathContextIdsList: MutableList<XLabeledPathContextIds<String>>) {
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