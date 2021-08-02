package config

import Language
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class Config(
    // ====== How to open repositories =====
    @SerialName("additional preprocessing") val additionalPreprocessing: PreprocessingConfig?,

    // ====== Pipeline configuration =====
    val filters: List<FilterConfig>,
    @SerialName("label") val labelExtractor: LabelExtractorConfig,
    val storage: StorageConfig,

    // ===== Parser configuration =====
    val language: Language,
    @SerialName("tree transformations") val treeTransformers: List<PsiTreeTransformationConfig>,

    // ===== Other parameters =====
    val batchSize: Int = 10_000,
    val printTrees: Boolean = false
)

@Serializable data class PreprocessingConfig(
    @SerialName("android sdk absolute path") val androidSdkPath: String
)
