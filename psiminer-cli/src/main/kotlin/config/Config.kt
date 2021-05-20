package config

import Language
import kotlinx.serialization.Serializable

@Serializable data class Config(
    // ====== Pipeline configuration =====
    val filters: List<FilterConfig>,
    val problem: ProblemConfig,
    val storage: StorageConfig,

    // ===== Parser configuration =====
    val languages: List<Language>,
    val ignoreRules: List<PsiNodeIgnoreRuleConfig>,

    // ===== Debugging =====
    val printTrees: Boolean = false
)
