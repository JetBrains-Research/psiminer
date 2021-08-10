package psi.transformations.excludenode

import BasePsiRequiredTest
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiElement
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.nodeProperties.isHidden
import psi.preOrder
import psi.transformations.PsiTreeTransformation
import kotlin.reflect.KClass

abstract class KotlinExcludeTransformationTest : BasePsiRequiredTest() {

    abstract val transformation: PsiTreeTransformation
    abstract val excludeType: KClass<out PsiElement>

    @ParameterizedTest
    @ValueSource(
        strings = ["abstractMethod", "overrideMethod", "emptyMethod", "smallMethod", "largeMethod", "recursiveMethod"]
    )
    fun `test ignoring in Kotlin methods`(methodName: String) = ReadAction.run<Exception> {
        val psiRoot = getKotlinMethod(methodName)
        transformation.transform(psiRoot)
        val notHiddenNodes = psiRoot.preOrder().count { excludeType.isInstance(it) && !it.isHidden }
        assertEquals(0, notHiddenNodes)
    }
}
