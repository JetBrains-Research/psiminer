package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import labelextractor.MethodNameLabelExtractor
import labelextractor.LabelExtractor
import labelextractor.MethodCommentLabelExtractor

@Serializable
abstract class LabelExtractorConfig {
    abstract fun createProblem(): LabelExtractor
}

@Serializable
@SerialName("method name")
class MethodNameLabelExtractorConfig : LabelExtractorConfig() {
    override fun createProblem(): LabelExtractor = MethodNameLabelExtractor()
}

@Serializable
@SerialName("method comment")
class MethodCommentLabelExtractorConfig : LabelExtractorConfig() {
    override fun createProblem(): LabelExtractor = MethodCommentLabelExtractor()
}