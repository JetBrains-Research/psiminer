package formatter

import labelextractor.LabeledTree

interface Formatter {
    fun format(labeledTree: LabeledTree): String
}
