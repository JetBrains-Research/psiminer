package psi.language

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod
import psi.language.method.JavaMethodProvider
import psi.language.method.MethodProvider
import psi.nodeIgnoreRules.JavaIgnoreRule
import psi.transformation.JavaTreeTransformer

class JavaHandler : LanguageHandler() {
    override val ignoreRuleType = JavaIgnoreRule::class
    override val treeTransformer = JavaTreeTransformer::class

    override val classPsiType = PsiClass::class.java
    override val methodPsiType = PsiMethod::class.java

    override val methodProvider: MethodProvider = JavaMethodProvider()
}
