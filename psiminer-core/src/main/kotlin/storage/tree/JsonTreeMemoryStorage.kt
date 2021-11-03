package storage.tree

import labelextractor.LabeledTree
import storage.MemoryStorage

/**
 * Store tree in JSONL format.
 * Format description: https://jsonlines.org
 * Tree saves in Python150K format: https://www.sri.inf.ethz.ch/py150
 */
class JsonTreeMemoryStorage(
    private val withPaths: Boolean
) : MemoryStorage() {
    override fun convert(labeledTree: LabeledTree): String {
        return convert(labeledTree, withPaths)
    }
}
