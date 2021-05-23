package problem

import GranularityLevel
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import psi.renameAllSubtreeOccurrences

class MethodNamePrediction : Problem {

    override val granularityLevel = GranularityLevel.Method

    override fun processTree(root: PsiElement): LabeledTree {
        if (root !is PsiMethod) throw IllegalArgumentException("Try to extract method name not from the method")
        val methodName = root.name
        renameAllSubtreeOccurrences(root, methodNameToken)
        return LabeledTree(root, methodName)
    }

    companion object {
        private const val methodNameToken = "METHOD_NAME"
    }
}
