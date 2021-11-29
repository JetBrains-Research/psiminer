import com.intellij.openapi.application.ReadAction
import org.junit.Assert
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class CommonTest : JavaPsiRequiredTest("JavaMethods") {

    private fun provideMethodsSizes(): Array<Arguments> = arrayOf(
        Arguments.of("smallMethod", 4), Arguments.of("largeMethod", 14)
    )

    @ParameterizedTest
    @MethodSource("provideMethodsSizes")
    fun `test counting lines in code`(methodName: String, methodLength: Int) = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        val codeLines = getCleanCode(psiRoot.text)
        Assert.assertEquals(methodLength, codeLines.size)
    }
}
