package config

import Language
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.research.pluginUtilities.preprocessing.PreprocessorManager
import org.jetbrains.research.pluginUtilities.preprocessing.getKotlinJavaPreprocessorManager

@Serializable
data class Config(
    // ====== How to open repositories =====
    @SerialName("additional preprocessing") val additionalPreprocessing: PreprocessingConfig = PreprocessingConfig(
        enable = false
    ),

    // ====== Pipeline configuration =====
    val filters: List<FilterConfig>,
    @SerialName("label") val labelExtractor: LabelExtractorConfig,
    val storage: StorageConfig,

    // ===== Parser configuration =====
    val language: Language,
    @SerialName("tree transformations") val treeTransformers: List<PsiTreeTransformationConfig>,

    // ===== Other parameters =====
    val batchSize: Int = 1,
    val printTrees: Boolean = false
)

@Serializable
data class PreprocessingConfig(
    val enable: Boolean = true,
    val androidSdkHome: String? = null
) {
    fun createPreprocessorManager(): PreprocessorManager? =
        if (enable) getKotlinJavaPreprocessorManager(androidSdkHome) else null
}
