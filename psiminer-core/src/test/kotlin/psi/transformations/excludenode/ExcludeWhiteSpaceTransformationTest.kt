package psi.transformations.excludenode

import BasePsiRequiredTest
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.nodeProperties.isHidden

internal class ExcludeWhiteSpaceTransformationTest : BasePsiRequiredTest() {

    private val excludeWhiteSpaceTransformation = ExcludeWhiteSpaceTransformation()

    @ParameterizedTest
    @ValueSource(
        strings = ["abstractMethod", "overrideMethod", "emptyMethod", "smallMethod", "largeMethod", "recursiveMethod"]
    )
    fun `test ignoring in Java methods`(methodName: String) = ReadAction.run<Exception> {
        val psiRoot = getJavaMethod(methodName)
        excludeWhiteSpaceTransformation.transform(psiRoot)
        val notHiddenWhitespaces = PsiTreeUtil
            .collectElementsOfType(psiRoot, PsiWhiteSpace::class.java)
            .filterNot { it.isHidden }
        assertEquals(0, notHiddenWhitespaces.size)
    }

    @ParameterizedTest
    @ValueSource(
        strings = ["abstractMethod", "overrideMethod", "emptyMethod", "smallMethod", "largeMethod", "recursiveMethod"]
    )
    fun `test ignoring in Kotlin methods`(methodName: String) = ReadAction.run<Exception> {
        val psiRoot = getKotlinMethod(methodName)
        excludeWhiteSpaceTransformation.transform(psiRoot)
        val notHiddenWhitespaces = PsiTreeUtil
            .collectElementsOfType(psiRoot, PsiWhiteSpace::class.java)
            .filterNot { it.isHidden }
        assertEquals(0, notHiddenWhitespaces.size)
    }
}
