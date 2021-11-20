package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import labelextractor.MethodNameLabelExtractor
import labelextractor.LabelExtractor

@Serializable
abstract class LabelExtractorConfig<T> {
    abstract fun createProblem(): LabelExtractor<T>
}

@Serializable
@SerialName("method name")
class MethodNameLabelExtractorConfig : LabelExtractorConfig<String>() {
    override fun createProblem(): LabelExtractor<String> = MethodNameLabelExtractor()
}
