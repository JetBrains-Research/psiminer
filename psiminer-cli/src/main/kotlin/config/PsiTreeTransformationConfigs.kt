package config

import Language
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import psi.transformations.*
import psi.transformations.typeresolve.JavaResolveTypeTransformation
import java.lang.IllegalArgumentException

@Serializable
abstract class PsiTreeTransformationConfig {
    open class UnsupportedLanguageTransformation(transformation: String, language: String) :
        IllegalArgumentException("Transformation $transformation doesn't support $language")

    abstract fun createTreeTransformation(language: Language): PsiTreeTransformation
}

@Serializable
@SerialName("hide literals")
class HideLiteralsTransformationConfig(
    private val hideNumbers: Boolean = false,
    private val numberWhiteList: List<Int> = listOf(0, 1, 32, 64),
    private val hideStrings: Boolean = false,
) : PsiTreeTransformationConfig() {
    override fun createTreeTransformation(language: Language): PsiTreeTransformation =
        when (language) {
            Language.Java -> JavaHideLiteralsTransformation(hideNumbers, numberWhiteList, hideStrings)
            else -> throw UnsupportedLanguageTransformation("hide literals", language.name)
        }
}

@Serializable
@SerialName("compress operators")
class CompressOperatorsTransformationConfig : PsiTreeTransformationConfig() {
    override fun createTreeTransformation(language: Language): PsiTreeTransformation =
        when (language) {
            Language.Java -> JavaCompressOperatorTransformation()
            else -> throw UnsupportedLanguageTransformation("compress operators", language.name)
        }
}

@Serializable
@SerialName("remove comments")
class RemoveCommentsTransformationConfig(private val removeDoc: Boolean = true) : PsiTreeTransformationConfig() {
    override fun createTreeTransformation(language: Language): PsiTreeTransformation =
        when (language) {
            Language.Java -> JavaRemoveCommentsTransformation(removeDoc)
            Language.GO -> GoRemoveCommentsTransformation()
            else -> throw UnsupportedLanguageTransformation("remove comments", language.name)
        }
}

@Serializable
@SerialName("resolve type")
class ResolveTypeTransformationConfig : PsiTreeTransformationConfig() {
    override fun createTreeTransformation(language: Language): PsiTreeTransformation = when (language) {
        Language.Java -> JavaResolveTypeTransformation()
        else -> throw UnsupportedLanguageTransformation("resolve type", language.name)
    }
}
