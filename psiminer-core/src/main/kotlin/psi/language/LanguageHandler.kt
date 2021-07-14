package psi.language

import GranularityLevel
import Language
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import psi.method.MethodProvider
import psi.transformations.PsiTreeTransformation

abstract class LanguageHandler {
    abstract val language: Language
    abstract val methodProvider: MethodProvider

    abstract val transformationType: Class<out PsiTreeTransformation>
    abstract val classPsiType: Class<out PsiElement>
    abstract val methodPsiType: Class<out PsiElement>

    /**
     * Abstract method to collect identifiers of functions that are called in subtree of root
     * @param root: root of tree
     * @return: list of PsiElement that correspond to identifiers of function calls
     */
    abstract fun collectFunctionCallsIdentifiers(root: PsiElement): List<PsiElement>

    fun splitByGranularity(psiFile: PsiFile, granularity: GranularityLevel): List<PsiElement> =
        when (granularity) {
            GranularityLevel.File -> listOf(psiFile)
            GranularityLevel.Class -> PsiTreeUtil.collectElementsOfType(psiFile, classPsiType).toList()
            GranularityLevel.Method -> PsiTreeUtil.collectElementsOfType(psiFile, methodPsiType).toList()
        }
}
