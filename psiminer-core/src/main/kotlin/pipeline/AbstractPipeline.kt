package pipeline

import Language
import filter.Filter
import labelextractor.LabelExtractor
import psi.Parser
import psi.language.JavaHandler
import psi.language.KotlinHandler
import psi.transformations.PsiTreeTransformation

abstract class AbstractPipeline(
    protected val language: Language,
    protected val labelExtractor: LabelExtractor,
    protected val filters: List<Filter>,
    psiTreeTransformations: List<PsiTreeTransformation>
) {

    protected val languageHandler = when (language) {
        Language.Java -> JavaHandler()
        Language.Kotlin -> KotlinHandler()
    }

    protected val parser = Parser(languageHandler, psiTreeTransformations, labelExtractor.granularityLevel)
}
