package config

import kotlinx.serialization.Serializable

@Serializable data class Config(
    // ====== Pipeline configuration =====
    val filters: List<FilterConfig>,
    val problem: ProblemConfig,
    val storage: StorageConfig,

    // ===== Parser configuration =====
    val parserParameters: ParserConfig,

    // ===== Debugging =====
    val printTrees: Boolean = false
)
