package problem

import GranularityLevel
import kotlinx.serialization.Serializable
import psi.PsiNode

data class LabeledTree(val root: PsiNode, val label: String)

interface Problem {
    val granularityLevel: GranularityLevel
    fun processTree(root: PsiNode): LabeledTree?
}
