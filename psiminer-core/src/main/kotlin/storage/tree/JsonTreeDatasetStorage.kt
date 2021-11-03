package storage.tree

import Dataset
import labelextractor.LabeledTree
import storage.DatasetStorage
import java.io.File

/**
 * Store each tree in JSONL format (one sample per line)
 * Format description: https://jsonlines.org
 * Tree saves in Python150K format: https://www.sri.inf.ethz.ch/py150
 */
class JsonTreeDatasetStorage(outputDirectory: File, private val withPaths: Boolean) : DatasetStorage(outputDirectory) {

    override val fileExtension: String = "jsonl"

    override fun convert(labeledTree: LabeledTree, holdout: Dataset?) = convert(labeledTree, withPaths)
}
