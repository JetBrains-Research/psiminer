package psi.language

import psi.nodeIgnoreRules.PsiNodeIgnoreRule
import kotlin.reflect.KClass

abstract class LanguageDescription {
    abstract val ignoreRuleType: KClass<out PsiNodeIgnoreRule>
}
