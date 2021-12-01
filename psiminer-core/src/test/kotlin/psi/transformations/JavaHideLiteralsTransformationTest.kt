package psi.transformations

import JavaPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.util.elementType
import org.junit.jupiter.api.Test
import psi.nodeProperties.token
import psi.preOrder
import psi.transformations.JavaHideLiteralsTransformation.Companion.NUMBER_LITERAL
import psi.transformations.JavaHideLiteralsTransformation.Companion.STRING_LITERAL

internal class JavaHideLiteralsTransformationTest : JavaPsiRequiredTest("JavaMethods") {

    @Test
    fun `test not hiding whitelist literals in Java method`() = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        val transformation = JavaHideLiteralsTransformation(true, numberWhiteList, true)
        transformation.transform(psiRoot)
        psiRoot.preOrder()
            .filter { it.elementType.toString() == "INTEGER_LITERAL" }
            .filter { numberWhiteList.contains(it.text.toInt()) }
            .forEach { assertEquals(it.text, it.token) }
    }

    @Test
    fun `test hiding int literals in Java method`() = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        val transformation = JavaHideLiteralsTransformation(true, numberWhiteList, true)
        transformation.transform(psiRoot)
        psiRoot.preOrder()
            .filter { it.elementType.toString() == "INTEGER_LITERAL" }
            .filter { !numberWhiteList.contains(it.text.toInt()) }
            .forEach { assertEquals(NUMBER_LITERAL, it.token) }
    }

    @Test
    fun `test hiding string literals in Java method`() = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        val transformation = JavaHideLiteralsTransformation(true, numberWhiteList, true)
        transformation.transform(psiRoot)
        psiRoot.preOrder()
            .filter { it.elementType.toString() == "STRING_LITERAL" }
            .forEach { assertEquals(STRING_LITERAL, it.token) }
    }

    companion object {
        private const val methodName = "largeMethod"
        private val numberWhiteList = listOf(0, 5)
    }
}
