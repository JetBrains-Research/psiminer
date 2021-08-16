package psi

import GranularityLevel
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import psi.language.LanguageHandler
import psi.transformations.CommonTreeTransformation
import psi.transformations.PsiTreeTransformation

class Parser(
    private val languageHandler: LanguageHandler,
    private val psiTreeTransformations: List<PsiTreeTransformation>,
    private val granularity: GranularityLevel
) {

    init {
        psiTreeTransformations.forEach {
            if (!languageHandler.transformationType.isInstance(it) && it !is CommonTreeTransformation) {
                throw IllegalArgumentException("Incorrect transformation ${it::class.simpleName}")
            }
        }
    }

    val language = languageHandler.language

    override fun toString(): String =
        "$language parser with ${psiTreeTransformations.joinToString { it::class.simpleName ?: "" }}"

    fun <T> parseFile(virtualFile: VirtualFile, psiManager: PsiManager, callback: (PsiElement) -> T): List<T> =
        ReadAction.compute<List<T>, Exception> {
            val psiFile = psiManager.findFile(virtualFile) ?: throw ParserException(virtualFile.path)
            psiTreeTransformations.forEach { it.transform(psiFile) }
            val granularityPsiElements = languageHandler.splitByGranularity(psiFile, granularity)
            granularityPsiElements.map { callback(it) }
        }
}

class ParserException(val filepath: String) : RuntimeException("Error while parsing $filepath file")
