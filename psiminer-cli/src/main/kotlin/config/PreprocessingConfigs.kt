package config

import JVMRepositoryOpener
import RepositoryOpener
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.research.pluginUtilities.preprocessing.getKotlinJavaPreprocessorManager

@Serializable
abstract class PreprocessingConfig {
    abstract fun createPreprocessing(): RepositoryOpener
}

@Serializable
@SerialName("dummy")
class DummyPreprocessingConfig : PreprocessingConfig() {
    override fun createPreprocessing(): RepositoryOpener = RepositoryOpener()
}

@Serializable
@SerialName("jvm")
class JVMPreprocessingConfig(private val androidSdkHome: String? = null) : PreprocessingConfig() {
    override fun createPreprocessing(): RepositoryOpener {
        val preprocessorManager = getKotlinJavaPreprocessorManager(androidSdkHome)
        return JVMRepositoryOpener(preprocessorManager)
    }
}
