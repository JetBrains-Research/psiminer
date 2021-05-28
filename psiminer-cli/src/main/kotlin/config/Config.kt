package config

import Language
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class Config(
    // ====== Pipeline configuration =====
    val filters: List<FilterConfig>,
    val problem: ProblemConfig,
    val storage: StorageConfig,

    // ===== Parser configuration =====
    val languages: List<Language>,
    @SerialName("ignore rules") val ignoreRules: List<PsiNodeIgnoreRuleConfig>,
    @SerialName("process tree") val treeProcessors: List<PsiTreeProcessorConfig>,

    // ===== Debugging =====
    val printTrees: Boolean = false
)
