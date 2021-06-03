package psi.language

import psi.nodeIgnoreRules.JavaIgnoreRule
import psi.nodeIgnoreRules.PsiNodeIgnoreRule
import psi.transformation.JavaTreeTransformer
import psi.transformation.PsiTreeTransformer
import kotlin.reflect.KClass

abstract class LanguageDescription {
    abstract val ignoreRuleType: KClass<out PsiNodeIgnoreRule>
    abstract val treeTransformer: KClass<out PsiTreeTransformer>
}

class JavaDescription : LanguageDescription() {
    override val ignoreRuleType = JavaIgnoreRule::class
    override val treeTransformer = JavaTreeTransformer::class
}
