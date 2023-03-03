package psi

import GranularityLevel
import PATH_KEY
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import labelextractor.LabeledTree
import psi.language.LanguageHandler
import psi.transformations.CommonTreeTransformation
import psi.transformations.PsiTreeTransformation
import java.io.File

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

    suspend fun parseFile(
        virtualFile: VirtualFile,
        project: Project,
        callback: (PsiElement) -> LabeledTree?,
        sendChannel: SendChannel<LabeledTree>
    ) {
            try {
                val granularityPsiElements = mutableListOf<PsiElement>()
                ReadAction.run<Exception> {
                    val psiManager = PsiManager.getInstance(project)
                    val psiFile = psiManager.findFile(virtualFile) ?: throw ParserException(virtualFile.path)
                    psiTreeTransformations.forEach { it.transform(psiFile) }
                    granularityPsiElements.addAll(languageHandler.splitByGranularity(psiFile, granularity))
                    granularityPsiElements.forEach {
                        val path = File(project.basePath ?: "")
                            .toPath()
                            .parent
                            .relativize(File(it.containingFile.virtualFile.path).toPath())
                        it.putUserData(PATH_KEY, path.toString())
                    }
                }
                granularityPsiElements.forEach {
                    callback(it)?.let { it1 -> sendChannel.send(it1) }
                }
            } catch (e: AssertionError) {
                println("Skipping file due to error in file parsing: ${e.message}")
            }
        }

//    suspend fun applyTransformations(file: VirtualFile, project: Project) {
//        val psiManager = PsiManager.getInstance(project)
//        val psiFile = psiManager.findFile(file) ?: throw ParserException(file.path)
//        ReadAction.run<Exception> {
//            psiTreeTransformations.forEach {
//                it.transform(psiFile)
//            }
//        }
//    }


}

class ParserException(val filepath: String) : RuntimeException("Error while parsing $filepath file")
