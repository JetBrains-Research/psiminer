package problem

import GranularityLevel
import psi.PsiNode

data class Sample(val root: PsiNode, val label: String)

interface Problem {
    val granularityLevel: GranularityLevel
    fun processTree(root: PsiNode): Sample?
}
