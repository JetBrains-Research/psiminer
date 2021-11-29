package filter

import JavaPsiRequiredTest
import KotlinPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.Assert
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

interface ModifierFilterTest {
    val modifierFilter: ModifierFilter
        get() = ModifierFilter(listOf("abstract"))

    fun provideAbstractMethods(): Array<Arguments> = arrayOf(
        Arguments.of("smallMethod", false), Arguments.of("abstractMethod", true)
    )
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JavaModifierFilterTest : ModifierFilterTest, JavaPsiRequiredTest("JavaMethods") {
    @ParameterizedTest
    @MethodSource("provideAbstractMethods")
    fun `test filtering java abstract methods`(methodName: String, isAbstract: Boolean) = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        Assert.assertEquals(!isAbstract, modifierFilter.validateTree(psiRoot, handler))
    }
}

class KotlinModifierFilterTest : ModifierFilterTest, KotlinPsiRequiredTest("KotlinMethods") {
    @ParameterizedTest
    @MethodSource("provideAbstractMethods")
    fun `test filtering kotlin abstract methods`(methodName: String, isAbstract: Boolean) = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        Assert.assertEquals(!isAbstract, modifierFilter.validateTree(psiRoot, handler))
    }
}

