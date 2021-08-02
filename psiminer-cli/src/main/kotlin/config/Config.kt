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
    val language: Language,
    @SerialName("tree transformations") val treeTransformers: List<PsiTreeTransformationConfig>,

    // ===== Other parameters =====
    val parseAsync: Boolean = false,
    val batchSize: Int? = null,
    val printTrees: Boolean = false
)
