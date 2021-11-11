package labelextractor

import BasePsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.Assert
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class DummyLabelExtractorTest : BasePsiRequiredTest() {

    private val dummyLabelExtractor = DummyLabelExtractor()

    @ParameterizedTest
    @ValueSource(strings = [simpleMethod, largeMethod, methodWithRecursion])
    fun `test java dummy extraction`(methodName: String) = ReadAction.run<Exception> {
        val psiRoot = getJavaMethod(methodName)
        val label = dummyLabelExtractor.handleTree(psiRoot, javaHandler)
        Assert.assertEquals("", label)
    }

    companion object {
        const val simpleMethod = "smallMethod"
        const val largeMethod = "largeMethod"
        const val methodWithRecursion = "recursiveMethod"
    }
}
