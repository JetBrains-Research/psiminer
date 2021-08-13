package filter

import BasePsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.Assert
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ConstructorFilterTest : BasePsiRequiredTest() {

    private val constructorFilter = ConstructorFilter()

    private fun provideJavaConstructors(): Array<Arguments> = arrayOf(
        Arguments.of("smallMethod", false), Arguments.of("Main", true)
    )

    @ParameterizedTest
    @MethodSource("provideJavaConstructors")
    fun validateTree(methodName: String, isConstructor: Boolean) = ReadAction.run<Exception> {
        val psiRoot = getJavaMethod(methodName)
        Assert.assertEquals(!isConstructor, constructorFilter.validateTree(psiRoot, javaHandler))
    }
}
