package config

import Language
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class Config(
    // ====== Pipeline configuration =====
    val filters: List<FilterConfig>,
    @SerialName("label") val labelExtractor: LabelExtractorConfig,
    val storage: StorageConfig,

    // ===== Parser configuration =====
    val languages: List<Language>,
    @SerialName("node ignore rules") val nodeIgnoreRules: List<PsiNodeIgnoreRuleConfig>,
    @SerialName("tree transformations") val treeTransformers: List<PsiTreeProcessorConfig>,

    // ===== Other parameters =====
    val batchSize: Int = 10_000,
    val printTrees: Boolean = false
)
