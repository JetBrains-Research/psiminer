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
     * Perform an action on the identifier node of recursive call, e.g. mask the real name.
     * @param root of the target method.
     * @param action to perform.
     */
    abstract fun actionOnRecursiveCallIdentifier(root: PsiElement, action: (PsiElement) -> Unit)

    fun splitByGranularity(psiElement: PsiElement, granularity: GranularityLevel): List<PsiElement> =
        when (granularity) {
            GranularityLevel.File -> listOf(psiElement)
            GranularityLevel.Class -> PsiTreeUtil.collectElementsOfType(psiElement, classPsiType).toList()
            GranularityLevel.Method -> PsiTreeUtil.collectElementsOfType(psiElement, methodPsiType).toList()
        }
}
