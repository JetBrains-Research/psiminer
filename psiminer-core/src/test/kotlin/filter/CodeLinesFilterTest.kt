package filter

import BasePsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.Assert
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class CodeLinesFilterTest : BasePsiRequiredTest() {

    private val codeLinesFilter = CodeLinesFilter(maxCodeLines = upBorder)

    private fun provideMethodsSizes(): Array<Arguments> = arrayOf(
        Arguments.of("smallMethod", 4), Arguments.of("largeMethod", 12)
    )

    @ParameterizedTest
    @MethodSource("provideMethodsSizes")
    fun `test accepting java method`(methodName: String, methodLength: Int) = ReadAction.run<Exception> {
        val psiRoot = getJavaMethod(methodName)
        Assert.assertEquals(methodLength <= upBorder, codeLinesFilter.validateTree(psiRoot, javaHandler))
    }

    @ParameterizedTest
    @MethodSource("provideMethodsSizes")
    fun `test accepting kotlin method`(methodName: String, methodLength: Int) = ReadAction.run<Exception> {
        val psiRoot = getKotlinMethod(methodName)
        Assert.assertEquals(methodLength <= upBorder, codeLinesFilter.validateTree(psiRoot, kotlinHandler))
    }

    companion object {
        const val upBorder = 10
    }
}
