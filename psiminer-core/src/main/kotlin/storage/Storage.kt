package storage

import Dataset
import psi.PsiNode
import java.io.File

interface Storage {
    val outputDirectory: File

    fun store(sample: PsiNode, label: String, holdout: Dataset)
    fun printStatistic()
    fun close()
}
