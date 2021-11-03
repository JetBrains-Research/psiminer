package pipeline

import Language
import com.intellij.psi.PsiElement
import filter.Filter
import labelextractor.LabelExtractor
import psi.printTree
import psi.transformations.PsiTreeTransformation
import storage.MemoryStorage

class InMemoryPipeline(
    language: Language,
    labelExtractor: LabelExtractor,
    filters: List<Filter>,
    psiTreeTransformations: List<PsiTreeTransformation>,
    private val memoryStorage: MemoryStorage
) : AbstractPipeline(
    language,
    labelExtractor,
    filters,
    psiTreeTransformations
) {
    fun processPsi(psiElement: PsiElement, printTrees: Boolean = false) {
        parser.parsePsi(psiElement, psiElement.project) { psiRoot ->
            if (filters.any { !it.validateTree(psiRoot, languageHandler) }) return@parsePsi
            val labeledTree =
                labelExtractor.extractLabel(psiRoot, languageHandler) ?: return@parsePsi
            memoryStorage.store(labeledTree)
            if (printTrees) labeledTree.root.printTree()
        }
    }
}
