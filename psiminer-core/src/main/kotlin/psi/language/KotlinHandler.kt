package psi.language

import Language
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtReferenceExpression
import psi.method.KotlinMethodProvider
import psi.transformations.KotlinTreeTransformation

class KotlinHandler : LanguageHandler() {
    override val language = Language.Kotlin
    override val methodProvider = KotlinMethodProvider()

    override val transformationType = KotlinTreeTransformation::class.java
    override val classPsiType = KtClass::class.java
    override val methodPsiType = KtNamedFunction::class.java

    override fun collectFunctionCallsIdentifiers(root: PsiElement): List<PsiElement> =
        PsiTreeUtil
            .collectElements(root) { it.elementType.toString() == "IDENTIFIER" }
            .filter { it.parent is KtReferenceExpression }
}
