package storage

import Config
import Dataset
import astminer.parse.antlr.SimpleNode
import java.io.File

interface Storage {
    val outputDirectory: File
    val config: Config

    fun store(sample: SimpleNode, label: String, holdout: Dataset)
    fun printStatistic()
    fun close()
}
