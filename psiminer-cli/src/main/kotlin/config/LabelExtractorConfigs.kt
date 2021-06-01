package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import labelextractor.MethodNamePrediction
import labelextractor.LabelExtractor

@Serializable
abstract class LabelExtractorConfig {
    abstract fun createProblem(): LabelExtractor
}

@Serializable
@SerialName("method name prediction")
class MethodNamePredictionConfig : LabelExtractorConfig() {
    override fun createProblem(): LabelExtractor = MethodNamePrediction()
}
