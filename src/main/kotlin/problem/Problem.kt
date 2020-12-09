package problem

import Dataset
import GranularityLevel
import psi.PsiNode

interface Problem {
    val granularityLevel: GranularityLevel
    fun processTree(root: PsiNode, holdout: Dataset)
}
