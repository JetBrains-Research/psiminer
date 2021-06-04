import filter.Filter
import labelextractor.LabelExtractor
import psi.nodeIgnoreRules.PsiNodeIgnoreRule
import psi.transformation.PsiTreeTransformer
import storage.Storage

data class PipelineConfig(
    val parameters: Parameters,

    val languages: List<Language>,
    val nodeIgnoreRules: List<PsiNodeIgnoreRule>,
    val treeTransformations: List<PsiTreeTransformer>,

    val filters: List<Filter>,
    val labelExtractor: LabelExtractor,
    val storage: Storage
)

data class Parameters(val batchSize: Int, val printTrees: Boolean)
