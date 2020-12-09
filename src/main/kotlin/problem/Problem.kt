package problem

import Dataset
import GranularityLevel
import astminer.parse.antlr.SimpleNode

interface Problem {
    val granularityLevel: GranularityLevel
    fun processTree(root: SimpleNode, holdout: Dataset)
}
