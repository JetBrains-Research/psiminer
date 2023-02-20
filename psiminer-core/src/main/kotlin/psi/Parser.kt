package psi

import GranularityLevel
import PATH_KEY
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import psi.language.LanguageHandler
import psi.transformations.PriTreeModifyingTransformation
import psi.transformations.PsiTreeTransformation
import java.io.File

class Parser(
    private val languageHandler: LanguageHandler,
    private val psiTreeTransformations: List<PsiTreeTransformation>,
    private val granularity: GranularityLevel
) {

    val language = languageHandler.language

    override fun toString(): String =
        "$language parser with ${psiTreeTransformations.joinToString { it::class.simpleName ?: "" }}"

    fun <T> parseFile(virtualFile: VirtualFile, project: Project, callback: (PsiElement) -> T): List<T> {
        return ReadAction.compute<List<T>, Exception> {
            try {
                val psiManager = PsiManager.getInstance(project)
                val psiFile = psiManager.findFile(virtualFile) ?: throw ParserException(virtualFile.path)
                val granularityPsiElements = languageHandler.splitByGranularity(psiFile, granularity)
                granularityPsiElements.map {
                    val path = File(project.basePath ?: "")
                        .toPath()
                        .parent
                        .relativize(File(it.containingFile.virtualFile.path).toPath())
                    it.putUserData(PATH_KEY, path.toString())
                    callback(it)
                }
            } catch (e: AssertionError) {
                println("Skipping file due to error in file parsing: ${e.message}")
                emptyList()
            }
        }
    }

    fun applyTransformations(file: VirtualFile, project: Project) {
        val psiManager = PsiManager.getInstance(project)
        val psiFile = psiManager.findFile(file) ?: throw ParserException(file.path)
        applyNonModifyingTransformations(psiFile)
        applyModifyingTransformations(psiFile, project)
    }

    private fun applyNonModifyingTransformations(psiFile: PsiFile) =
        ReadAction.run<Exception> {
            psiTreeTransformations.filter { it !is PriTreeModifyingTransformation }.forEach {
                it.transform(psiFile)
            }
        }

    private fun applyModifyingTransformations(psiFile: PsiFile, project: Project) =
        ApplicationManager.getApplication().invokeAndWait {
            WriteCommandAction.runWriteCommandAction(project) {
                psiTreeTransformations.filter { it is PriTreeModifyingTransformation }.forEach {
                    it.transform(psiFile)
                }
            }
        }
}

class ParserException(val filepath: String) : RuntimeException("Error while parsing $filepath file")
