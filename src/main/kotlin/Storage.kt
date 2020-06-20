import astminer.common.model.PathContext
import astminer.common.storage.*
import net.openhft.chronicle.map.ChronicleMap
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


const val DEFAULT_FRAGMENTS_PER_BATCH = 1_000L

data class XPathContext(val startTokenType: String, val pathContext: PathContext, val endTokenType: String)
data class XPathContextId(
    val startTokenTypeId: Long,
    val startTokenId: Long,
    val pathId: Long,
    val endTokenId: Long,
    val endTokenTypeId: Long
)

data class XLabeledPathContexts<T>(val label: T, val pathContexts: Collection<XPathContext>)
//data class XLabeledPathContextIds<T>(val label: T, val pathContexts: () -> Collection<XPathContextId>?)
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
    databaseName: String,
    private val outputFolderPath: String,
    val batchMode: Boolean = true,
    val fragmentsPerBatch: Long = DEFAULT_FRAGMENTS_PER_BATCH) : XPathStorage<LabelType> {

    protected val labeledPathContextIdsMap: Map<Dataset, MutableList<XLabeledPathContextIds<LabelType>>> =
        mapOf(
            Dataset.Train to mutableListOf(),
            Dataset.Test to mutableListOf(),
            Dataset.Val to mutableListOf()
        )

    private val streams: Map<Dataset, () -> FileOutputStream>

    val startTime = LocalDateTime.now()
    val startTimeFormatted = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"))

    private var tokensTypesMapId = 0L
    private val tokensTypesMap by lazy {
        ChronicleMap
            .of(CharSequence::class.java, java.lang.Long::class.java)
            .name("tokensTypesMap")
            .entries(10_000_000)
            .averageKey("List<DaemonInitialConnectException>")
            .create()
            // .createPersistedTo(File("$outputFolderPath/${databaseName}_${startTimeFormatted}__chronicle__token_type.dat"))
    }
    private var tokensMapId = 0L
    private val tokensMap by lazy {
        ChronicleMap
            .of(CharSequence::class.java, java.lang.Long::class.java)
            .name("tokensMap")
            .entries(10_000_000)
            .averageKey("pluggablescmmaterialconfigvalidateconcretematerial")
            .create()
        // .createPersistedTo(File("$outputFolderPath/${databaseName}_${startTimeFormatted}__chronicle__token_type.dat"))
    }
    private var orientedNodeTypesMapId = 0L
    private val orientedNodeTypesMap by lazy {
        ChronicleMap
            .of(CharSequence::class.java, java.lang.Long::class.java)
            .name("orientedNodeTypesMap")
            .entries(10_000_000)
            .averageKey("NAME_VALUE_PAIR|LITERAL_EXPRESSION|STRING_LITERAL DOWN")
            .create()
        // .createPersistedTo(File("$outputFolderPath/${databaseName}_${startTimeFormatted}__chronicle__token_type.dat"))
    }
    private var pathsMapId = 0L
    private val pathsMap by lazy {
        ChronicleMap
            .of(CharSequence::class.java, java.lang.Long::class.java)
            .name("pathsMap")
            .entries(10_000_000)
            .averageKey("16 192 73 76 12 13 77 83 8")
            .create()
        // .createPersistedTo(File("$outputFolderPath/${databaseName}_${startTimeFormatted}__chronicle__token_type.dat"))
    }

    init {
        File(outputFolderPath).mkdirs()
        streams = Dataset.values().map {
            val f = File("$outputFolderPath/${it.name.toLowerCase()}_${startTimeFormatted}_path_contexts.csv")
            if (f.exists()) error("${f.absolutePath} already exists, terminating!")
            f.createNewFile()
            it to { FileOutputStream(f, true) }
        }.toMap()
    }

    private fun dumpTokenTypeStorage(file: File) {
        xDumpIdStorageToCsv(tokensTypesMap, "token_type", file)
    }

    private fun dumpTokenStorage(file: File) {
        xDumpIdStorageToCsv(tokensMap, "token", file)
    }

    private fun dumpOrientedNodeTypesStorage(file: File) {
        xDumpIdStorageToCsv(orientedNodeTypesMap, "node_type", file)
    }

    private fun dumpPathsStorage(file: File) {
        xDumpIdStorageToCsv(pathsMap, "path", file)
    }

    abstract fun dumpPathContexts(file: () -> FileOutputStream, tokensLimit: Long, pathsLimit: Long, labeledPathContextIdsList: MutableList<XLabeledPathContextIds<LabelType>>)

    private fun doStore(xpathContext: XPathContext): XPathContextId {
        // Start
        val startTokenTypeId = tokensTypesMap.getOrPut(xpathContext.startTokenType, {
            java.lang.Long(++tokensTypesMapId)
        }).toLong()

        val startTokenId = tokensMap.getOrPut(xpathContext.pathContext.startToken, {
            java.lang.Long(++tokensMapId)
        }).toLong()

        // End
        val endTokenTypeId = tokensTypesMap.getOrPut(xpathContext.endTokenType, {
            java.lang.Long(++tokensTypesMapId)
        }).toLong()

        val endTokenId = tokensMap.getOrPut(xpathContext.pathContext.endToken, {
            java.lang.Long(++tokensMapId)
        }).toLong()

        // Path
        val orientedNodesIds = xpathContext.pathContext.orientedNodeTypes.map {
            orientedNodeTypesMap.getOrPut(orientedNodeToCsvString(it), {
                java.lang.Long(++orientedNodeTypesMapId)
            }).toLong()
        }
        val pathId = pathsMap.getOrPut(pathToCsvString(orientedNodesIds)) {
            java.lang.Long(++pathsMapId)
        }.toLong()

        return XPathContextId(startTokenTypeId, startTokenId, pathId, endTokenId, endTokenTypeId)
    }

    private fun dumpPathContextsIfNeeded(dataset: Dataset) {
        val labeledPathContextIdsList = labeledPathContextIdsMap[dataset] ?: error("unknown dataset type: $dataset")
        val stream = streams[dataset] ?: error("unknown dataset type: $dataset")

        if (!batchMode || labeledPathContextIdsList.size < fragmentsPerBatch) {
            return
        }
        File(outputFolderPath).mkdirs()

        dumpPathContexts(
            stream,
            Long.MAX_VALUE, Long.MAX_VALUE,
            labeledPathContextIdsList
        )
        labeledPathContextIdsList.clear()

        File(outputFolderPath).mkdirs()
    }

    // private val cache = mutableMapOf<LabelType, Collection<XPathContextId>>()
    // private val postponed = mutableMapOf<LabelType, Collection<XPathContext>>()

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
        println("\tdumping token types...")
        dumpTokenTypeStorage(File("$outputFolderPath/${startTimeFormatted}_tokens_types.csv"))
        println("\tdumping tokens...")
        dumpTokenStorage(File("$outputFolderPath/${startTimeFormatted}_tokens.csv"))
        println("\tdumping oriented node types...")
        dumpOrientedNodeTypesStorage(File("$outputFolderPath/${startTimeFormatted}_node_types.csv"))
        println("\tdumping paths...")
        dumpPathsStorage(File("$outputFolderPath/${startTimeFormatted}_paths.csv"))

        labeledPathContextIdsMap.forEach { (dataset, labeledPathContextIdsList) ->
            val stream = streams[dataset] ?: error("unknown dataset $dataset")
            if (!batchMode) {
                dumpPathContexts(stream, tokensLimit, pathsLimit, labeledPathContextIdsList)
            } else {
                dumpPathContexts(
                    stream,
                    Long.MAX_VALUE, Long.MAX_VALUE,
                    labeledPathContextIdsList
                )
            }
        }
    }
}


class XCode2VecPathStorage(databaseName: String, outputFolderPath: String) : XCountingPathStorage<String>(databaseName, outputFolderPath = outputFolderPath, batchMode = true) {
    override fun dumpPathContexts(file: () -> FileOutputStream, tokensLimit: Long, pathsLimit: Long, labeledPathContextIdsList: MutableList<XLabeledPathContextIds<String>>) {
        val lines = mutableListOf<String>()
        labeledPathContextIdsList
        .forEach { labeledPathContextIds ->
            val pathContextIdsString = labeledPathContextIds
                .pathContexts
                .joinToString(separator = " ") { pathContextId ->
                    "${pathContextId.startTokenTypeId},${pathContextId.startTokenId},${pathContextId.pathId},${pathContextId.endTokenId},${pathContextId.endTokenTypeId}"
                }

            lines.add("${labeledPathContextIds.label} $pathContextIdsString")
        }

        file().bufferedWriter().use { out ->
            lines.forEach {
                out.append(it)
                out.newLine()
            }
        }
    }
}

fun <K, V> xDumpIdStorageToCsv(storage: ChronicleMap<K, V>, typeHeader: String, file: File) {
    file.bufferedWriter().use { out ->
        out.append("id,$typeHeader")
        out.newLine()

        storage.forEach {
            val id = it.value
            val item = it.key
            out.append("$id,$item")
            out.newLine()
        }
    }

    storage.close()
}

//class XRankedIncrementalIdStorage<T>(private val collectionName: String, private val averageKey = ) {
//    private var keyCounter = 0L
//    private val idPerItem = ChronicleMap
//        .of(CharSequence::class.java, java.lang.Long::class.java)
//        .name("tokenType")
//        .entries(100_000_000)
//        .averageKey("List<DaemonInitialConnectException>")
//        .create()
//        // .createPersistedTo(File("$outputFolderPath/${databaseName}_${startTimeFormatted}__chronicle__token_type.dat"))
//
//    fun record(item: T): Long {
//        return idPerItem.getOrCreate(item) {
//            java.lang.Long(++keyCounter)
//        }.toLong()
//    }
//}
