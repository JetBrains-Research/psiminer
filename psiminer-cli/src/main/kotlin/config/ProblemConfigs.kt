package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import problem.MethodNamePrediction
import problem.Problem

@Serializable
abstract class ProblemConfig {
    abstract fun createProblem(): Problem
}

@Serializable
@SerialName("method name prediction")
class MethodNamePredictionConfig : ProblemConfig() {
    override fun createProblem(): Problem = MethodNamePrediction()
}
