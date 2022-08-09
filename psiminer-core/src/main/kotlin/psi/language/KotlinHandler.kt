package psi.language

import Language
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.nj2k.postProcessing.resolve
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import psi.assignment.AssignmentProvider
import psi.assignment.KotlinAssignmentProvider
import psi.method.KotlinMethodProvider
import psi.transformations.KotlinTreeTransformation

class KotlinHandler : LanguageHandler() {
    override val language = Language.Kotlin
    override val methodProvider = KotlinMethodProvider()
    override val assignmentProvider = KotlinAssignmentProvider()

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

    override fun actionOnRecursiveCallIdentifier(root: PsiElement, action: (PsiElement) -> Unit) {
        PsiTreeUtil.collectElements(root) { it is KtCallExpression }
            .filter {
                val resolved = (it as KtCallExpression).resolveToElement
                PsiManager.getInstance(root.project).areElementsEquivalent(root, resolved)
            }
            .forEach { callExpr ->
                // There is only one node with IDENTIFIER element type in call expression subtree.
                // Maybe there is a better way to retrieve it.
                PsiTreeUtil.collectElements(callExpr) { it.elementType.toString() == "IDENTIFIER" }.forEach(action)
            }
    }
}
