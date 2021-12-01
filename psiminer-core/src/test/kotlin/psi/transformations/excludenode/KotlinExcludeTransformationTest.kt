package psi.transformations.excludenode

import KotlinPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiElement
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.nodeProperties.isHidden
import psi.preOrder
import psi.transformations.PsiTreeTransformation
import kotlin.reflect.KClass

abstract class KotlinExcludeTransformationTest : KotlinPsiRequiredTest("KotlinMethods") {

    abstract val transformation: PsiTreeTransformation
    abstract val excludeType: KClass<out PsiElement>

    @ParameterizedTest
    @ValueSource(
        strings = ["abstractMethod", "emptyMethod", "smallMethod", "largeMethod", "recursiveMethod"]
    )
    fun `test ignoring in Kotlin methods`(methodName: String) = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        transformation.transform(psiRoot)
        val notHiddenNodes = psiRoot.preOrder().count { excludeType.isInstance(it) && !it.isHidden }
        assertEquals(0, notHiddenNodes)
    }
}
