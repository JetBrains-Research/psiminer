package psi.language

import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import psi.language.method.KotlinMethodProvider
import psi.language.method.MethodProvider
import psi.nodeIgnoreRules.JavaIgnoreRule
import psi.transformation.JavaTreeTransformer

class KotlinHandler : LanguageHandler() {
    override val ignoreRuleType = JavaIgnoreRule::class
    override val treeTransformer = JavaTreeTransformer::class

    override val classPsiType = KtClass::class.java
    override val methodPsiType = KtFunction::class.java

    override val methodProvider: MethodProvider = KotlinMethodProvider()
}