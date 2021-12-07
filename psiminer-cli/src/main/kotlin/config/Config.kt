package config

import Language
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    // ====== How to open repositories =====
    @SerialName("preprocessing") val preprocessing: PreprocessingConfig = DummyPreprocessingConfig(),

    // ====== Pipeline configuration =====
    val filters: List<FilterConfig>,
    @SerialName("label") val labelExtractor: LabelExtractorConfig<@Polymorphic Any>,
    val storage: StorageConfig,

    // ===== Parser configuration =====
    val language: Language,
    @SerialName("tree transformations") val treeTransformers: List<PsiTreeTransformationConfig>,

    // ===== Other parameters =====
    val numThreads: Int = 1,
    val printTrees: Boolean = false
)
