package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import psi.nodeIgnoreRules.*

@Serializable
abstract class PsiNodeIgnoreRuleConfig {
    abstract fun createIgnoreRule(): PsiNodeIgnoreRule
}

// ===== Common ignore rules =====

@Serializable
@SerialName("whitespace")
class WhiteSpaceIgnoreRuleConfig : PsiNodeIgnoreRuleConfig() {
    override fun createIgnoreRule(): PsiNodeIgnoreRule = WhiteSpaceIgnoreRule()
}

@Serializable
@SerialName("keywords")
class KeywordsIgnoreRuleConfig : PsiNodeIgnoreRuleConfig() {
    override fun createIgnoreRule(): PsiNodeIgnoreRule = KeywordIgnoreRule()
}

@Serializable
@SerialName("empty grammar lists")
class EmptyListsIgnoreRuleConfig : PsiNodeIgnoreRuleConfig() {
    override fun createIgnoreRule(): PsiNodeIgnoreRule = EmptyListsIgnoreRule()
}

// ===== Java ignore rules =====

@Serializable
@SerialName("package")
class PackageStatementIgnoreRuleConfig : PsiNodeIgnoreRuleConfig() {
    override fun createIgnoreRule(): PsiNodeIgnoreRule = PackageStatementIgnoreRule()
}

@Serializable
@SerialName("imports")
class ImportStatementIgnoreRuleConfig : PsiNodeIgnoreRuleConfig() {
    override fun createIgnoreRule(): PsiNodeIgnoreRule = ImportStatementIgnoreRule()
}

@Serializable
@SerialName("java symbols")
class JavaSymbolsIgnoreRuleConfig : PsiNodeIgnoreRuleConfig() {
    override fun createIgnoreRule(): PsiNodeIgnoreRule = JavaSymbolsIgnoreRule()
}
