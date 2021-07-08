package filter

import BasePsiRequiredTest
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiMethod
import org.junit.Assert
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class CodeLinesFilterTest : BasePsiRequiredTest(getResourcesRootPath(CodeLinesFilterTest::class)) {

    private val codeLinesFilter = CodeLinesFilter(maxCodeLines = upBorder)
    private val javaMethods = mutableMapOf<String, PsiMethod>()

    private fun provideJavaMethodsSizes(): Array<Arguments> = arrayOf(
        Arguments.of("smallMethod", 4), Arguments.of("largeMethod", 12)
    )

    @BeforeAll
    override fun setUp() {
        super.setUp()
        javaMethods.putAll(getAllJavaMethods(javaMethodsFile, myFixture))
    }

    @ParameterizedTest
    @MethodSource("provideJavaMethodsSizes")
    fun `test counting lines of code`(methodName: String, methodLength: Int) = ReadAction.run<Exception> {
        val psiRoot = javaMethods[methodName] ?: throw UnknownMethodException(methodName, javaMethodsFile)
        val codeLines = codeLinesFilter.getCleanCode(psiRoot.text)
        Assert.assertEquals(methodLength, codeLines.size)
    }

    @ParameterizedTest
    @MethodSource("provideJavaMethodsSizes")
    fun `test accepting method`(methodName: String, methodLength: Int) = ReadAction.run<Exception> {
        val psiRoot = javaMethods[methodName] ?: throw UnknownMethodException(methodName, javaMethodsFile)
        Assert.assertEquals(methodLength <= upBorder, codeLinesFilter.validateTree(psiRoot, javaHandler))
    }

    companion object {
        const val javaMethodsFile = "JavaMethods.java"
        const val upBorder = 10
    }
}