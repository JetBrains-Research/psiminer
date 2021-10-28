package psi.language

import Language
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import psi.method.KotlinMethodProvider
import psi.transformations.KotlinTreeTransformation

class KotlinHandler : LanguageHandler() {
    override val language = Language.Kotlin
    override val methodProvider = KotlinMethodProvider()

    override val transformationType = KotlinTreeTransformation::class.java
    override val classPsiType = KtClass::class.java
    override val methodPsiType = KtFunction::class.java

    private val KtExpression.resolveToElement: PsiElement?
        get() {
            return when (this) {
                is KtCallExpression -> referenceExpression()?.resolve()
                is KtReferenceExpression -> this.resolve()
                else -> null
            }
        }

    override fun actionOnRecursiveCall(root: PsiElement, action: (PsiElement) -> Unit) {
        PsiTreeUtil.collectElements(root) { it is KtCallExpression }
            .filter {
                val resolved = (it as KtCallExpression).resolveToElement
                PsiManager.getInstance(root.project).areElementsEquivalent(root, resolved)
            }
            .forEach { callExpr ->
                PsiTreeUtil.collectElements(callExpr) { it.elementType.toString() == "IDENTIFIER" }.forEach(action)
            }
    }
}
