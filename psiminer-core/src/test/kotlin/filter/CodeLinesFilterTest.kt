package filter

import JavaPsiRequiredTest
import KotlinPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.Assert
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

interface CodeLinesFilterTest {
    val codeLinesFilter: CodeLinesFilter
        get() = CodeLinesFilter(maxCodeLines = upBorder)

    fun provideMethodsSizes(): Array<Arguments> = arrayOf(
        Arguments.of("smallMethod", 4), Arguments.of("largeMethod", 12)
    )

    companion object {
        const val upBorder = 10
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JavaCodeLinesFilterTest : CodeLinesFilterTest, JavaPsiRequiredTest("JavaMethods") {
    @ParameterizedTest
    @MethodSource("provideMethodsSizes")
    fun `test accepting java method by code lines`(methodName: String, methodLength: Int) = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        Assert.assertEquals(
            methodLength <= CodeLinesFilterTest.upBorder,
            codeLinesFilter.validateTree(psiRoot, handler)
        )
    }
}

class KotlinCodeLinesFilterTest : CodeLinesFilterTest, KotlinPsiRequiredTest("KotlinMethods") {
    @ParameterizedTest
    @MethodSource("provideMethodsSizes")
    fun `test accepting kotlin method by code lines`(methodName: String, methodLength: Int) =
        ReadAction.run<Exception> {
            val psiRoot = getMethod(methodName)
            Assert.assertEquals(
                methodLength <= CodeLinesFilterTest.upBorder,
                codeLinesFilter.validateTree(psiRoot, handler)
            )
        }
}
