package psi.language

import Language
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import psi.method.JavaMethodProvider
import psi.transformations.JavaTreeTransformation

class JavaHandler : LanguageHandler() {
    override val language = Language.Java

    override val transformationType = JavaTreeTransformation::class.java
    override val methodProvider = JavaMethodProvider()

    override val classPsiType = PsiClass::class.java
    override val methodPsiType = PsiMethod::class.java
}
