package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import psi.treeProcessors.CompressOperators
import psi.treeProcessors.HideLiterals
import psi.treeProcessors.PsiTreeProcessor
import psi.treeProcessors.RemoveComments

@Serializable
abstract class PsiTreeProcessorConfig {
    abstract fun createTreeProcessor(): PsiTreeProcessor
}

@Serializable
@SerialName("hide literals")
class HideLiteralsConfig(
    private val hideNumbers: Boolean = false,
    private val numberWhiteList: List<Int> = listOf(0, 1, 32, 64),
    private val hideStrings: Boolean = false,
) : PsiTreeProcessorConfig() {
    override fun createTreeProcessor(): PsiTreeProcessor = HideLiterals(hideNumbers, numberWhiteList, hideStrings)
}

@Serializable
@SerialName("compress operators")
class CompressOperatorsConfig : PsiTreeProcessorConfig() {
    override fun createTreeProcessor(): PsiTreeProcessor = CompressOperators()
}

@Serializable
@SerialName("remove comments")
class RemoveCommentsConfig(private val removeJavaDoc: Boolean = true) : PsiTreeProcessorConfig() {
    override fun createTreeProcessor(): PsiTreeProcessor = RemoveComments(removeJavaDoc)
}
