import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.kotlin.psi.KtFunction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import psi.language.JavaHandler
import psi.language.KotlinHandler
import java.io.File
import kotlin.jvm.Throws

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BasePsiRequiredTest : BasePlatformTestCase() {

    protected val javaHandler = JavaHandler()
    protected val kotlinHandler = KotlinHandler()

    private val javaMethods = mutableMapOf<String?, PsiElement>()
    private val kotlinMethods = mutableMapOf<String?, PsiElement>()

    private class ResourceException(resourceRoot: String) : RuntimeException("Can't find resources in $resourceRoot")

    // We should define the root resources folder
    @Throws(ResourceException::class)
    override fun getTestDataPath(): String =
        BasePsiRequiredTest::class.java.getResource(resourcesRoot)?.path ?: throw ResourceException(resourcesRoot)

    @BeforeAll
    override fun setUp() {
        super.setUp()
        val javaMethodsFile = File(testDataPath).resolve(javaMethodsFileName)
        javaMethods.putAll(getAllMethods(javaMethodsFile, javaHandler, myFixture))
        val kotlinMethodsFile = File(testDataPath).resolve(kotlinMethodsFileName)
        kotlinMethods.putAll(getAllMethods(kotlinMethodsFile, kotlinHandler, myFixture))
    }

    @AfterAll
    override fun tearDown() {
        super.tearDown()
    }

    private class UnknownMethodException(methodName: String, fileName: String) :
        RuntimeException("Can't find method $methodName in $fileName")

    protected fun getJavaMethod(methodName: String): PsiElement =
        javaMethods[methodName] ?: throw UnknownMethodException(methodName, javaMethodsFileName)

    protected fun getKotlinMethod(methodName: String): PsiElement =
        kotlinMethods[methodName] as KtFunction

    companion object {
        // We can not get the root of the class resources automatically
        private const val resourcesRoot: String = "data"

        internal const val javaMethodsFileName = "JavaMethods.java"
        internal const val kotlinMethodsFileName = "KotlinMethods.kt"
    }
}
