package filter

import BasePsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.Assert

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class AnnotationFilterTest : BasePsiRequiredTest() {

    private val annotationFilter = AnnotationFilter(listOf("Override"))

    private fun provideOverrideMethods(): Array<Arguments> = arrayOf(
        Arguments.of("smallMethod", false), Arguments.of("overrideMethod", true)
    )

    @ParameterizedTest
    @MethodSource("provideOverrideMethods")
    fun `test filtering java override methods`(methodName: String, isOverride: Boolean) = ReadAction.run<Exception> {
        val psiRoot = getJavaMethod(methodName)
        Assert.assertEquals(!isOverride, annotationFilter.validateTree(psiRoot, javaHandler))
    }

    @ParameterizedTest
    @MethodSource("provideOverrideMethods")
    fun `test filtering kotlin override methods`(methodName: String, isOverride: Boolean) = ReadAction.run<Exception> {
        val psiRoot = getKotlinMethod(methodName)
        Assert.assertEquals(!isOverride, annotationFilter.validateTree(psiRoot, kotlinHandler))
    }
}
