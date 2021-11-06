package filter

import BasePsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.Assert
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class EmptyMethodFilterTest : BasePsiRequiredTest() {
    private val emptyMethodFilter = EmptyMethodFilter()

    private fun provideEmptyMethods(): Array<Arguments> = arrayOf(
        Arguments.of("smallMethod", false),
        Arguments.of("emptyMethod", true),
        Arguments.of("abstractMethod", true)
    )

    @ParameterizedTest
    @MethodSource("provideEmptyMethods")
    fun `test filtering java empty methods`(methodName: String, isEmpty: Boolean) = ReadAction.run<Exception> {
        val psiRoot = getJavaMethod(methodName)
        Assert.assertEquals(!isEmpty, emptyMethodFilter.validateTree(psiRoot, javaHandler))
    }

    @ParameterizedTest
    @MethodSource("provideEmptyMethods")
    fun `test filtering kotlin empty methods`(methodName: String, isEmpty: Boolean) = ReadAction.run<Exception> {
        val psiRoot = getKotlinMethod(methodName)
        Assert.assertEquals(!isEmpty, emptyMethodFilter.validateTree(psiRoot, kotlinHandler))
    }
}
