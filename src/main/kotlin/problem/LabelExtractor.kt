package problem

import GranularityLevel
import psi.PsiNode

data class Sample(val root: PsiNode, val label: String)

interface LabelExtractor {
    val granularityLevel: GranularityLevel
    fun processTree(root: PsiNode): Sample?
}
