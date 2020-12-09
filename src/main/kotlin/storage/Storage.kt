package storage

import Config
import Dataset
import psi.PsiNode
import java.io.File

interface Storage {
    val outputDirectory: File
    val config: Config

    fun store(sample: PsiNode, label: String, holdout: Dataset)
    fun printStatistic()
    fun close()
}
