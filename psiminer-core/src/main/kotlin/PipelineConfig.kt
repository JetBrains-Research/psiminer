import filter.Filter
import labelextractor.LabelExtractor
import psi.transformations.PsiTreeTransformation
import storage.Storage

data class PipelineConfig(
    val parameters: Parameters,

    val language: Language,
    val psiTreeTransformations: List<PsiTreeTransformation>,

    val filters: List<Filter>,
    val labelExtractor: LabelExtractor,
    val storage: Storage
)

data class Parameters(val batchSize: Int, val printTrees: Boolean)
