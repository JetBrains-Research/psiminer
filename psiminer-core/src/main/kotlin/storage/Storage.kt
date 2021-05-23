package storage

import Dataset
import Language
import problem.LabeledTree
import java.io.File

interface Storage {
    val outputDirectory: File

    fun store(labeledTree: LabeledTree, holdout: Dataset, language: Language)
    fun printStatistic()
    fun close()
}
