package storage

import Dataset
import labelextractor.LabeledTree
import java.io.File
import java.io.PrintWriter

abstract class Storage

abstract class DatasetStorage(protected val outputDirectory: File) : Storage() {

    private val datasetFileWriters = mutableMapOf<Dataset?, PrintWriter>()
    private val datasetStatistic = mutableMapOf<Dataset?, Int>()

    init {
        outputDirectory.mkdirs()
    }

    abstract val fileExtension: String
    protected abstract fun convert(labeledTree: LabeledTree, holdout: Dataset?): String

    fun store(labeledTree: LabeledTree, holdout: Dataset?) {
        val stringRepresentation = convert(labeledTree, holdout)

        datasetStatistic[holdout] = datasetStatistic.getOrPut(holdout) { 0 }.plus(1)
        datasetFileWriters.getOrPut(holdout) {
            val outputFilename = holdout?.folderName ?: "result"
            val outputFile = outputDirectory.resolve("$outputFilename.$fileExtension")
            outputFile.parentFile.mkdirs()
            outputFile.createNewFile()
            PrintWriter(outputFile)
        }.println(stringRepresentation)
    }

    fun store(labeledTrees: List<LabeledTree>, holdout: Dataset?) =
        labeledTrees.forEach { store(it, holdout) }

    open fun printStatistic() =
        datasetStatistic.forEach {
            val prefix = "${it.value} samples"
            val suffix = if (it.key != null) " in ${it.key} holdout" else ""
            println("$prefix$suffix")
        }

    open fun close() = datasetFileWriters.forEach { it.value.close() }
}

abstract class MemoryStorage : Storage() {
    private val _collected = mutableListOf<String>()
    val collected: List<String>
        get() = _collected

    fun store(labeledTree: LabeledTree) {
        _collected.add(convert(labeledTree))
    }

    protected abstract fun convert(labeledTree: LabeledTree): String
}
