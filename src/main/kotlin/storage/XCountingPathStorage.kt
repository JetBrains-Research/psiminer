package storage

import Dataset
import astminer.common.model.OrientedNodeType
import astminer.common.storage.RankedIncrementalIdStorage
import astminer.common.storage.dumpIdStorageToCsv
import astminer.common.storage.orientedNodeToCsvString
import astminer.common.storage.pathToCsvString
import java.io.File
import java.io.PrintWriter

abstract class XCountingPathStorage<LabelType>(
    final override val directoryPath: String,
    final override val noTypes: Boolean
) : XPathContextsStorage<LabelType> {

    protected val typesMap: RankedIncrementalIdStorage<String> = RankedIncrementalIdStorage()
    protected val tokensMap: RankedIncrementalIdStorage<String> = RankedIncrementalIdStorage()
    protected val pathsMap: RankedIncrementalIdStorage<List<Long>> = RankedIncrementalIdStorage()
    protected val orientedNodeTypesMap: RankedIncrementalIdStorage<OrientedNodeType> = RankedIncrementalIdStorage()

    private val datasetPathsFiles = mutableMapOf<Dataset, File>()
    private val datasetFileWriters = mutableMapOf<Dataset, PrintWriter>()

    init {
        File(directoryPath).mkdir()
        Dataset.values().forEach {
            val file = File("$directoryPath/path_contexts.${it.folderName}.csv")
            file.createNewFile()
            datasetPathsFiles[it] = file
            datasetFileWriters[it] = PrintWriter(file)
        }
    }

    abstract fun xPathContextIdsToString(xPathContextIds: List<XPathContextId>, label: LabelType): String

    private fun dumpXPathContexts(xLabeledPathContextIds: XLabeledPathContextIds<LabelType>, dataset: Dataset) {
        datasetFileWriters[dataset]?.println(
            xPathContextIdsToString(
                xLabeledPathContextIds.xPathContexts as List<XPathContextId>,
                xLabeledPathContextIds.label
            )
        )
    }

    private fun storeXPathContext(xPathContext: XPathContext): XPathContextId {
        val startTokenId = tokensMap.record(xPathContext.pathContext.startToken)
        val startTypeId = typesMap.record(xPathContext.startTokenType)

        val orientedNodesIds = xPathContext.pathContext.orientedNodeTypes.map { orientedNodeTypesMap.record(it) }
        val pathId = pathsMap.record(orientedNodesIds)

        val endTokenId = tokensMap.record(xPathContext.pathContext.endToken)
        val endTypeId = typesMap.record(xPathContext.endTokenType)
        return XPathContextId(startTypeId, startTokenId, pathId, endTypeId, endTokenId)
    }

    override fun store(xLabeledPathContexts: XLabeledPathContexts<LabelType>, dataset: Dataset) {
        val xLabeledPathContextIds = XLabeledPathContextIds(
            xLabeledPathContexts.label,
            xLabeledPathContexts.xPathContexts.map { storeXPathContext(it) }
        )
        dumpXPathContexts(xLabeledPathContextIds, dataset)
    }

    override fun close() {
        dumpIdStorageToCsv(tokensMap, "token", { it }, File("$directoryPath/tokens.csv"), Long.MAX_VALUE)
        dumpIdStorageToCsv(
            orientedNodeTypesMap, "node_type", orientedNodeToCsvString,
            File("$directoryPath/node_types.csv"), Long.MAX_VALUE
        )
        dumpIdStorageToCsv(pathsMap, "path", pathToCsvString, File("$directoryPath/paths.csv"), Long.MAX_VALUE)
        dumpIdStorageToCsv(typesMap, "token_type", { it }, File("$directoryPath/token_types.csv"), Long.MAX_VALUE)

        datasetFileWriters.forEach {
            it.value.close()
        }
    }
}
