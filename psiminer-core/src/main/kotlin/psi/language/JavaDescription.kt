package psi.language

import psi.nodeIgnoreRules.JavaIgnoreRule

class JavaDescription : LanguageDescription() {
    override val ignoreRuleType = JavaIgnoreRule::class
}