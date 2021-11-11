package psi

import GranularityLevel
import PATH_KEY
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import psi.language.LanguageHandler
import psi.transformations.CommonTreeTransformation
import psi.transformations.PsiTreeTransformation
import java.nio.file.Paths

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

    fun <T> parseFile(virtualFile: VirtualFile, project: Project, callback: (PsiElement) -> T): List<T> {
        val psiFile = ReadAction.compute<PsiFile, Exception> {
            val psiManager = PsiManager.getInstance(project)
            psiManager.findFile(virtualFile) ?: throw ParserException(virtualFile.path)
        }
        return parsePsi(psiFile, project, callback)
    }

    fun <T> parsePsi(psiElement: PsiElement, project: Project, callback: (PsiElement) -> T): List<T> =
        ReadAction.compute<List<T>, Exception> {
            psiTreeTransformations.forEach { it.transform(psiElement) }
            val granularityPsiElements = languageHandler.splitByGranularity(psiElement, granularity)
            granularityPsiElements.map { element ->
                val relativeFilePath = element.containingFile
                    ?.virtualFile
                    ?.path
                    ?.let { Paths.get(it) }
                val projectPath = Paths.get(project.basePath ?: "").parent
                val filePath = relativeFilePath?.let { projectPath.relativize(it) }

                element.putUserData(PATH_KEY, filePath.toString())
                callback(element)
            }
        }
}

class ParserException(val filepath: String) : RuntimeException("Error while parsing $filepath file")
