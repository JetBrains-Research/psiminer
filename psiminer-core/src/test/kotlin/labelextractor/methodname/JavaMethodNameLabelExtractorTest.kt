package labelextractor.methodname

import JavaPsiRequiredTest
import astminer.common.splitToSubtokens
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.util.PsiTreeUtil
import labelextractor.MethodNameLabelExtractor
import labelextractor.MethodNameLabelExtractor.Companion.METHOD_NAME
import org.junit.Assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.nodeProperties.token

internal class JavaMethodNameLabelExtractorTest : JavaPsiRequiredTest("JavaMethods") {

    private val methodNameLabelExtractor = MethodNameLabelExtractor()

    @ParameterizedTest
    @ValueSource(strings = [simpleMethod, methodWithRecursion])
    fun `test method name extraction`(methodName: String) = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        val methodNameExtracted = methodNameLabelExtractor.handleTree(psiRoot, handler)
        val methodNameNormalized = splitToSubtokens(methodName).joinToString("|")
        Assert.assertEquals(methodNameNormalized, methodNameExtracted)
    }

    @Test
    fun `test method name hiding`() = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodWithRecursion)
        val methodNameNormalized = splitToSubtokens(methodWithRecursion).joinToString("|")
        methodNameLabelExtractor.extractLabel(psiRoot, handler)
        val hiddenNodes = PsiTreeUtil.collectElements(psiRoot) { it.token == METHOD_NAME }
        val nonHiddenNodes = PsiTreeUtil.collectElements(psiRoot) { it.token == methodNameNormalized }
        Assert.assertEquals(2, hiddenNodes.size)
        Assert.assertEquals(1, nonHiddenNodes.size)
    }

    @Test
    fun `test no recursion no masking`() = ReadAction.run<Exception> {
        val psiRoot = getMethod(largeMethod)
        methodNameLabelExtractor.extractLabel(psiRoot, handler)
        val hiddenNodes = PsiTreeUtil.collectElements(psiRoot) { it.token == METHOD_NAME }
        // Since there is no recursion in the function, only the method name itself should be masked.
        Assert.assertEquals(1, hiddenNodes.size)
    }

    @Test
    fun `test dot call with same name no masking`() = ReadAction.run<Exception> {
        val psiRoot = getMethod(dotCallMethod)
        methodNameLabelExtractor.extractLabel(psiRoot, handler)
        val hiddenNodes = PsiTreeUtil.collectElements(psiRoot) { it.token == METHOD_NAME }
        Assert.assertEquals(1, hiddenNodes.size)
    }

    companion object {
        const val simpleMethod = "smallMethod"
        const val largeMethod = "largeMethod"
        const val methodWithRecursion = "recursiveMethod"
        const val dotCallMethod = "sizeOf"
    }
}
