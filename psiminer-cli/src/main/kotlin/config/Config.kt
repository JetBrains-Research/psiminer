package config

import Language
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.research.pluginUtilities.preprocessing.PreprocessorManager
import org.jetbrains.research.pluginUtilities.preprocessing.android.AndroidSdkPreprocessor
import org.jetbrains.research.pluginUtilities.preprocessing.common.DeleteFilesPreprocessor

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
    val parseAsync: Boolean = false,
    val batchSize: Int? = null,
    val printTrees: Boolean = false
)

@Serializable
data class PreprocessingConfig(
    val enable: Boolean = true,
    val androidSdk: String? = null
) {
    fun createPreprocessorManager(): PreprocessorManager? {
        // TODO: use getKotlinJavaPreprocessorManager. Do not accept PR if this todo is not resolved
        return if (enable) {
            val preprocessors = listOfNotNull(
                androidSdk?.let { AndroidSdkPreprocessor(it) },
                DeleteFilesPreprocessor(listOf(IDEA_FOLDER_NAME))
            )
            PreprocessorManager(preprocessors)
        } else {
            null
        }
    }

    companion object {
        const val IDEA_FOLDER_NAME = ".idea"
    }
}
