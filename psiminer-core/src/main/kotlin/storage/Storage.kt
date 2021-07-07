package storage

import Dataset
import Language
import com.jetbrains.rd.util.getOrCreate
import labelextractor.LabeledTree
import java.io.File
import java.io.PrintWriter

abstract class Storage(protected val outputDirectory: File) {

    protected data class OutputDirection(val holdout: Dataset?, val language: Language)
    private val datasetFileWriters = mutableMapOf<OutputDirection, PrintWriter>()
    private val datasetStatistic = mutableMapOf<OutputDirection, Int>()

    init {
        outputDirectory.mkdirs()
    }

    abstract val fileExtension: String
    protected abstract fun convert(labeledTree: LabeledTree, outputDirection: OutputDirection): String

    fun store(labeledTree: LabeledTree, holdout: Dataset?, language: Language) {
        val outputDirection = OutputDirection(holdout, language)
        val stringRepresentation = convert(labeledTree, outputDirection)

        datasetStatistic[outputDirection] = datasetStatistic.getOrCreate(outputDirection) { 0 }.plus(1)
        datasetFileWriters.getOrPut(outputDirection) {
            val outputFilename = holdout?.folderName ?: "result"
            val outputFile = outputDirectory
                .resolve(language.name)
                .resolve("$outputFilename.$fileExtension")
            outputFile.parentFile.mkdirs()
            outputFile.createNewFile()
            PrintWriter(outputFile)
        }.println(stringRepresentation)
    }

    fun store(labeledTrees: List<LabeledTree>, holdout: Dataset?, language: Language) =
        labeledTrees.forEach { store(it, holdout, language) }

    open fun printStatistic() =
        datasetStatistic.forEach {
            val prefix = "${it.value} samples for ${it.key.language}"
            val suffix = if (it.key.holdout != null) " in ${it.key.holdout} holdout" else ""
            println("$prefix$suffix")
        }

    open fun close() = datasetFileWriters.forEach { it.value.close() }
}
