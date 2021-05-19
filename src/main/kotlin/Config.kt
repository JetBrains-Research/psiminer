import filter.FilterConfig
import kotlinx.serialization.Serializable
import problem.ProblemConfig
import storage.StorageConfig

@Serializable data class Config(
    val filters: List<FilterConfig>,
    val problem: ProblemConfig,
    val storage: StorageConfig,

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
    Debugging
     */
    val printTrees: Boolean = false
)
