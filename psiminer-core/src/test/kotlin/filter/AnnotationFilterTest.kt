package filter

import JavaPsiRequiredTest
import KotlinPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.Assert
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

interface AnnotationFilterBase {
    val annotationFilter: AnnotationFilter
        get() = AnnotationFilter(listOf("Override"))

    fun provideOverrideMethods(): Array<Arguments> = arrayOf(
        Arguments.of("overrideMethod", true),
        Arguments.of("nonAnnotated", false),
        Arguments.of("customAnnotatedMethod", false)
    )
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JavaAnnotationFilterTest : AnnotationFilterBase, JavaPsiRequiredTest("JavaAnnotatedMethods") {
    @ParameterizedTest
    @MethodSource("provideOverrideMethods")
    fun `test filtering java override methods`(methodName: String, isOverride: Boolean) = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        Assert.assertEquals(!isOverride, annotationFilter.validateTree(psiRoot, handler))
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KotlinAnnotationFilterTest : AnnotationFilterBase, KotlinPsiRequiredTest("KotlinAnnotatedMethods") {
    @ParameterizedTest
    @MethodSource("provideOverrideMethods")
    fun `test filtering kotlin override methods`(methodName: String, isOverride: Boolean) = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        Assert.assertEquals(!isOverride, annotationFilter.validateTree(psiRoot, handler))
    }
}
