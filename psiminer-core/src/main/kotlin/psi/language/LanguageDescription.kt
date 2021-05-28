package psi.language

import psi.nodeIgnoreRules.JavaIgnoreRule
import psi.nodeIgnoreRules.PsiNodeIgnoreRule
import psi.treeProcessors.JavaTreeProcessor
import psi.treeProcessors.PsiTreeProcessor
import kotlin.reflect.KClass

abstract class LanguageDescription {
    abstract val ignoreRuleType: KClass<out PsiNodeIgnoreRule>
    abstract val treeProcessor: KClass<out PsiTreeProcessor>
}

class JavaDescription : LanguageDescription() {
    override val ignoreRuleType = JavaIgnoreRule::class
    override val treeProcessor = JavaTreeProcessor::class
}
