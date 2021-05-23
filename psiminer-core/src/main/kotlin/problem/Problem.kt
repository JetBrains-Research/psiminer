package problem

import GranularityLevel
import com.intellij.psi.PsiElement

data class LabeledTree(val root: PsiElement, val label: String)

interface Problem {
    val granularityLevel: GranularityLevel
    fun processTree(root: PsiElement): LabeledTree?
}
