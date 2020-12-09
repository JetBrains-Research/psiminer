import kotlinx.serialization.Serializable

@Serializable data class Config(
    val format: String,
    val problem: String,

    val resolveTypes: Boolean,
    val splitNames: Boolean = true,
    val resolvedTypesFirst: Boolean = false,

    val nodesToNumbers: Boolean = false,

    val batchSize: Int = 10_000,

    val maxTreeSize: Int? = null,

    val maxPathWidth: Int? = 4,
    val maxPathLength: Int? = 9,
    val maxPathsInTrain: Int? = null,
    val maxPathsInTest: Int? = null,
)
