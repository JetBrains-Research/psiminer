package problem

import GranularityLevel
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import psi.nodeProperties.technicalToken
import psi.nodeProperties.token
import psi.renameAllSubtreeOccurrences

class MethodNamePrediction : Problem {

    override val granularityLevel = GranularityLevel.Method

    override fun processTree(root: PsiElement): LabeledTree? {
        if (root !is PsiMethod) throw IllegalArgumentException("Try to extract method name not from the method")
        val methodName = root.nameIdentifier?.token ?: return null
        root.renameAllSubtreeOccurrences(METHOD_NAME) // Rename text representation
        PsiTreeUtil.collectElements(root) { it.textMatches(METHOD_NAME) } // Mark with technical token
            .forEach { it.technicalToken = METHOD_NAME }
        return LabeledTree(root, methodName)
    }

    private companion object {
        const val METHOD_NAME = "METHOD_NAME"
    }
}
