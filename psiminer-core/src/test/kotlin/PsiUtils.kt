import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import psi.language.LanguageHandler
import java.io.File

private fun getPsiFile(file: File, fixture: CodeInsightTestFixture): PsiFile = fixture.configureByFile(file.path)

fun getAllMethods(file: File, handler: LanguageHandler, fixture: CodeInsightTestFixture): Map<String?, PsiElement> =
    getPsiFile(file, fixture)
        .let {
            ReadAction.compute<Map<String?, PsiElement>, Exception> {
                handler
                    .splitByGranularity(it as PsiElement, GranularityLevel.Method)
                    .associateBy { handler.methodProvider.getNameNode(it).text }
            }
        }
