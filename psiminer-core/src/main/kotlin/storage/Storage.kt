package storage

import Dataset
import Language
import com.jetbrains.rd.util.getOrCreate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import problem.LabeledTree
import java.io.File
import java.io.PrintWriter

abstract class Storage(private val outputDirectory: File) {

    private data class OutputDirection(val holdout: Dataset, val language: Language)
    private val datasetFileWriters = mutableMapOf<OutputDirection, PrintWriter>()
    private val datasetStatistic = mutableMapOf<OutputDirection, Int>()

    init {
        outputDirectory.mkdirs()
    }

    abstract val fileExtension: String
    abstract fun convert(labeledTree: LabeledTree): String

    fun store(labeledTree: LabeledTree, holdout: Dataset, language: Language) {
        val stringRepresentation = convert(labeledTree)

        val outputDirection = OutputDirection(holdout, language)
        datasetStatistic[outputDirection] = datasetStatistic.getOrCreate(outputDirection) { 0 }.plus(1)
        datasetFileWriters.getOrPut(outputDirection) {
            val outputFile = outputDirectory
                .resolve(language.name)
                .resolve("${holdout.folderName}.${fileExtension}")
            outputFile.parentFile.mkdirs()
            outputFile.createNewFile()
            PrintWriter(outputFile)
        }.println(Json.encodeToString(stringRepresentation))
    }

    fun printStatistic() =
        datasetStatistic.forEach { println("${it.value} samples for ${it.key.language} in ${it.key.holdout} holdout") }

    fun close() = datasetFileWriters.forEach { it.value.close() }
}

