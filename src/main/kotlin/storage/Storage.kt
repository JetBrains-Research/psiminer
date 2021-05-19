package storage

import Dataset
import kotlinx.serialization.Serializable
import psi.PsiNode
import java.io.File

@Serializable
abstract class StorageConfig {
    abstract fun createStorage(outputDirectory: File): Storage
}

interface Storage {
    val config: StorageConfig
    val outputDirectory: File

    fun store(sample: PsiNode, label: String, holdout: Dataset)
    fun printStatistic()
    fun close()
}
