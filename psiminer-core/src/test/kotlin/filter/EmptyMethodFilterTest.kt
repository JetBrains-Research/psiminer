package filter

import JavaPsiRequiredTest
import KotlinPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.Assert
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

interface EmptyMethodFilterTest {
    val emptyMethodFilter: EmptyMethodFilter
        get() = EmptyMethodFilter()

    fun provideEmptyMethods(): Array<Arguments> = arrayOf(
        Arguments.of("interfaceMethod", true),
        Arguments.of("abstractMethod", true),
        Arguments.of("bracketsInline", true),
        Arguments.of("bracketsOneLine", true),
        Arguments.of("bracketsTwoLines", true),
        Arguments.of("commentInline", true),
        Arguments.of("singleComment", true),
        Arguments.of("multipleComments", true),
        Arguments.of("notEmptyMethod", false),
        Arguments.of("notEmptyMethodInline", false)
    )
}

class JavaEmptyMethodFilterTest : EmptyMethodFilterTest, JavaPsiRequiredTest("JavaEmptyMethods") {
    @ParameterizedTest
    @MethodSource("provideEmptyMethods")
    fun `test filtering java empty methods`(methodName: String, isEmpty: Boolean) = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        Assert.assertEquals(!isEmpty, emptyMethodFilter.validateTree(psiRoot, handler))
    }
}

class KotlinEmptyMethodsFilterTest : EmptyMethodFilterTest, KotlinPsiRequiredTest("KotlinEmptyMethods") {
    @ParameterizedTest
    @MethodSource("provideEmptyMethods")
    fun `test filtering kotlin empty methods`(methodName: String, isEmpty: Boolean) = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        Assert.assertEquals(!isEmpty, emptyMethodFilter.validateTree(psiRoot, handler))
    }
}
