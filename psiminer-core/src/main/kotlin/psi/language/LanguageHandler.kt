package psi.language

import GranularityLevel
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import psi.language.method.MethodProvider
import psi.nodeIgnoreRules.PsiNodeIgnoreRule
import psi.transformation.PsiTreeTransformer
import kotlin.reflect.KClass

abstract class LanguageHandler {
    abstract val ignoreRuleType: KClass<out PsiNodeIgnoreRule>
    abstract val treeTransformer: KClass<out PsiTreeTransformer>

    abstract val classPsiType: Class<out PsiElement>
    abstract val methodPsiType: Class<out PsiElement>

    abstract val methodProvider: MethodProvider

    fun splitByGranularity(psiFile: PsiFile, granularity: GranularityLevel): List<PsiElement> =
        when (granularity) {
            GranularityLevel.File -> listOf(psiFile)
            GranularityLevel.Class -> PsiTreeUtil.collectElementsOfType(psiFile, classPsiType).toList()
            GranularityLevel.Method -> PsiTreeUtil.collectElementsOfType(psiFile, methodPsiType).toList()
        }
}
