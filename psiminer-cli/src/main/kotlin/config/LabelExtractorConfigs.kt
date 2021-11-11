package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import labelextractor.DummyLabelExtractor
import labelextractor.LabelExtractor
import labelextractor.MethodNameLabelExtractor

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
@SerialName("dummy")
class DummyLabelExtractorConfig : LabelExtractorConfig() {
    override fun createProblem(): LabelExtractor = DummyLabelExtractor()
}
