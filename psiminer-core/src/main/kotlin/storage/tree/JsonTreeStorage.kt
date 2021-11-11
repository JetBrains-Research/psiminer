package storage.tree

import Dataset
import formatter.tree.JsonTreeFormatter
import labelextractor.LabeledTree
import storage.Storage
import java.io.File

/**
 * Store each tree in JSONL format (one sample per line)
 * Format description: https://jsonlines.org
 * Tree saves in Python150K format: https://www.sri.inf.ethz.ch/py150
 */
class JsonTreeStorage(
    outputDirectory: File,
    withPaths: Boolean,
    withRanges: Boolean
) : Storage(outputDirectory) {

    override val fileExtension: String = "jsonl"

    private val jsonTreeFormatter = JsonTreeFormatter(withPaths, withRanges)

    override fun convert(labeledTree: LabeledTree, holdout: Dataset?) = jsonTreeFormatter.format(labeledTree)
}
