package filter

import BasePsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.Assert

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ModifierFilterTest : BasePsiRequiredTest() {

    private val modifierFilter = ModifierFilter(listOf("abstract"))

    private fun provideAbstractMethods(): Array<Arguments> = arrayOf(
        Arguments.of("smallMethod", false), Arguments.of("abstractMethod", true)
    )

    @ParameterizedTest
    @MethodSource("provideAbstractMethods")
    fun `test filtering java abstract methods`(methodName: String, isAbstract: Boolean) = ReadAction.run<Exception> {
        val psiRoot = getJavaMethod(methodName)
        Assert.assertEquals(!isAbstract, modifierFilter.validateTree(psiRoot, javaHandler))
    }

    @ParameterizedTest
    @MethodSource("provideAbstractMethods")
    fun `test filtering kotlin abstract methods`(methodName: String, isAbstract: Boolean) = ReadAction.run<Exception> {
        val psiRoot = getKotlinMethod(methodName)
        Assert.assertEquals(!isAbstract, modifierFilter.validateTree(psiRoot, kotlinHandler))
    }
}
