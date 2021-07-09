import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import psi.language.JavaHandler
import java.io.File
import kotlin.reflect.KClass

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BasePsiRequiredTest(private val testDataRoot: String) : BasePlatformTestCase() {

    @BeforeAll
    override fun setUp() {
        super.setUp()
    }

    @AfterAll
    override fun tearDown() {
        super.tearDown()
    }

    protected val javaHandler = JavaHandler()

    private fun getPsiFile(fileName: String, fixture: CodeInsightTestFixture): PsiFile {
        val file = File(testDataPath).resolve(fileName)
        return fixture.configureByFile(file.path)
    }

    fun getAllJavaMethods(fileName: String, fixture: CodeInsightTestFixture): Map<String, PsiMethod> =
        getPsiFile(fileName, fixture)
            .let {
                ReadAction.compute<Map<String, PsiMethod>, Exception> {
                    javaHandler
                        .splitByGranularity(it, GranularityLevel.Method)
                        .associate {
                            val psiMethod = it as PsiMethod
                            psiMethod.name to psiMethod
                        }
                }
            }

    // We should define the root resources folder
    override fun getTestDataPath() = testDataRoot

    class UnknownMethodException(methodName: String, fileName: String) :
        RuntimeException("Can't find method $methodName in $fileName")

    companion object {
        // We can not get the root of the class resources automatically
        private const val resourcesRoot: String = "data"

        class ResourceFindingException(clsName: String?) : RuntimeException("Can't find resources for $clsName")

        fun getResourcesRootPath(cls: KClass<out BasePsiRequiredTest>): String =
            cls.java.getResource(resourcesRoot)?.path ?: throw ResourceFindingException(cls.simpleName)
    }
}
