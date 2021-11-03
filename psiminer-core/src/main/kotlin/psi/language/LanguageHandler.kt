package psi.language

import GranularityLevel
import Language
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import psi.method.MethodProvider
import psi.transformations.CommonTreeTransformation
import psi.transformations.PsiTreeTransformation

abstract class LanguageHandler {
    abstract val language: Language
    abstract val methodProvider: MethodProvider

    open val transformationType: Class<out PsiTreeTransformation> = CommonTreeTransformation::class.java
    abstract val classPsiType: Class<out PsiElement>
    abstract val methodPsiType: Class<out PsiElement>

    /**
     * Abstract method to collect identifiers of functions that are called in subtree of root
     * @param root: root of tree
     * @return: list of PsiElement that correspond to identifiers of function calls
     */
    abstract fun collectFunctionCallsIdentifiers(root: PsiElement): List<PsiElement>

    fun splitByGranularity(psiElement: PsiElement, granularity: GranularityLevel): List<PsiElement> =
        when (granularity) {
            GranularityLevel.File -> listOf(psiElement)
            GranularityLevel.Class -> PsiTreeUtil.collectElementsOfType(psiElement, classPsiType).toList()
            GranularityLevel.Method -> PsiTreeUtil.collectElementsOfType(psiElement, methodPsiType).toList()
        }
}
