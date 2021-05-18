import kotlinx.serialization.Serializable
import storage.StorageConfig

@Serializable data class Config(
    val storage: StorageConfig,
    /*
    Pipeline parameters
     */
    val problem: String,

    /*
    Parser parameters
     */
    val resolveTypes: Boolean = true,
    val splitNames: Boolean = true,
    val batchSize: Int = 10_000,
    val removeKeyword: Boolean = false,
    val compressOperators: Boolean = false,
    val removeComments: Boolean = true,
    val removeJavaDoc: Boolean = true,
    val compressTree: Boolean = false,

    /*
    Filters and them parameters
     */
    val filters: List<String> = listOf(),
    val minTreeSize: Int = 0,
    val maxTreeSize: Int? = null,
    val minCodeLength: Int = 0,
    val maxCodeLength: Int? = null,

    /*
    Debugging
     */
    val printTrees: Boolean = false
)
