package storage

import Dataset
import labelextractor.LabeledTree
import java.io.File
import java.io.PrintWriter

abstract class Storage(val outputDirectory: File) {
    private val datasetFileWriters = mutableMapOf<Dataset?, PrintWriter>()
    private val datasetStatistic = mutableMapOf<Dataset?, Long>()

    init {
        outputDirectory.mkdirs()
    }

    abstract val fileExtension: String
    abstract fun convert(labeledTree: LabeledTree, holdout: Dataset?): String

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
