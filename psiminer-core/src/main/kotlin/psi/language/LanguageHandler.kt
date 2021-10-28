package psi.language

import GranularityLevel
import Language
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
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
     * An abstract method to perform action on each recursion call inside the tree
     * @param root: root of tree
     * @param action: perfomed action
     */
    abstract fun actionOnRecursiveCall(root: PsiElement, action: (PsiElement) -> Unit)

    fun splitByGranularity(psiFile: PsiFile, granularity: GranularityLevel): List<PsiElement> =
        when (granularity) {
            GranularityLevel.File -> listOf(psiFile)
            GranularityLevel.Class -> PsiTreeUtil.collectElementsOfType(psiFile, classPsiType).toList()
            GranularityLevel.Method -> PsiTreeUtil.collectElementsOfType(psiFile, methodPsiType).toList()
        }
}
