package psi.language

import Language
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import psi.method.JavaMethodProvider
import psi.transformations.JavaTreeTransformation

class JavaHandler : LanguageHandler() {
    override val language = Language.Java
    override val methodProvider = JavaMethodProvider()

    override val transformationType = JavaTreeTransformation::class.java
    override val classPsiType = PsiClass::class.java
    override val methodPsiType = PsiMethod::class.java

    override fun actionOnRecursiveCall(root: PsiElement, action: (PsiElement) -> Unit) =
        PsiTreeUtil.collectElementsOfType(root, PsiMethodCallExpression::class.java)
            .filter {
                val resolvedMethod = it.resolveMethod()
                PsiManager.getInstance(root.project).areElementsEquivalent(root, resolvedMethod)
            }
            .forEach {
                it.methodExpression.lastChild.apply(action)
            }
}
