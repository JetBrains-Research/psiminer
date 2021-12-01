package filter

import JavaPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.Assert
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ConstructorFilterTest : JavaPsiRequiredTest("JavaMethods") {

    private val constructorFilter = ConstructorFilter()

    private fun provideJavaConstructors(): Array<Arguments> = arrayOf(
        Arguments.of("smallMethod", false), Arguments.of("Main", true)
    )

    @ParameterizedTest
    @MethodSource("provideJavaConstructors")
    fun validateTree(methodName: String, isConstructor: Boolean) = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        Assert.assertEquals(!isConstructor, constructorFilter.validateTree(psiRoot, handler))
    }
}
