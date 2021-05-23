package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import psi.nodeIgnoreRules.PsiNodeIgnoreRule
import psi.nodeIgnoreRules.WhiteSpaceIgnoreRule

@Serializable
abstract class PsiNodeIgnoreRuleConfig {
    abstract fun createIgnoreRule(): PsiNodeIgnoreRule
}

@Serializable
@SerialName("whitespace")
class WhitespaceIgnoreRuleConfig : PsiNodeIgnoreRuleConfig() {
    override fun createIgnoreRule(): PsiNodeIgnoreRule = WhiteSpaceIgnoreRule()
}
