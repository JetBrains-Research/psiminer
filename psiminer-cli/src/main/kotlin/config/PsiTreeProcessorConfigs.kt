package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import psi.transformation.CompressOperators
import psi.transformation.HideLiterals
import psi.transformation.PsiTreeTransformer
import psi.transformation.RemoveComments

@Serializable
abstract class PsiTreeProcessorConfig {
    abstract fun createTreeProcessor(): PsiTreeTransformer
}

@Serializable
@SerialName("hide literals")
class HideLiteralsConfig(
    private val hideNumbers: Boolean = false,
    private val numberWhiteList: List<Int> = listOf(0, 1, 32, 64),
    private val hideStrings: Boolean = false,
) : PsiTreeProcessorConfig() {
    override fun createTreeProcessor(): PsiTreeTransformer = HideLiterals(hideNumbers, numberWhiteList, hideStrings)
}

@Serializable
@SerialName("compress operators")
class CompressOperatorsConfig : PsiTreeProcessorConfig() {
    override fun createTreeProcessor(): PsiTreeTransformer = CompressOperators()
}

@Serializable
@SerialName("remove comments")
class RemoveCommentsConfig(private val removeJavaDoc: Boolean = true) : PsiTreeProcessorConfig() {
    override fun createTreeProcessor(): PsiTreeTransformer = RemoveComments(removeJavaDoc)
}
