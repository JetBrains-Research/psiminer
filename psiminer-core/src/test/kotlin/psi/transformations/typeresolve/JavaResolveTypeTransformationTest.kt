package psi.transformations.typeresolve

import BasePsiRequiredTest
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiKeyword
import com.intellij.psi.util.PsiTreeUtil
import org.junit.jupiter.api.Test

internal class JavaResolveTypeTransformationTest : BasePsiRequiredTest() {

    @Test
    fun `test resolve Java identifiers types transformation`() = ReadAction.run<Exception> {
        val psiRoot = getJavaMethod(javaMethod)
        val transformation = JavaResolveTypeTransformation()
        transformation.transform(psiRoot)
        val identifiers = PsiTreeUtil.collectElementsOfType(psiRoot, PsiIdentifier::class.java)
        tokenToType.forEach { (token, type) ->
            identifiers.filter { it.textMatches(token) }.forEach {
                assertEquals(
                    "token ${it.text}, type ${it.resolvedTokenType}", type, it.resolvedTokenType
                )
            }
        }
    }

    @Test
    fun `test resolve Java keywords types transformation`() = ReadAction.run<Exception> {
        val psiRoot = getJavaMethod(javaMethod)
        val transformation = JavaResolveTypeTransformation()
        transformation.transform(psiRoot)
        PsiTreeUtil.collectElementsOfType(psiRoot, PsiKeyword::class.java).forEach {
            assertEquals(KEYWORD, it.resolvedTokenType)
        }
    }

    companion object {
        const val javaMethod = "largeMethod"
        private val tokenToType = hashMapOf(
            "largeMethod" to "void",
            "x" to "int",
            "y" to "int",
            "mySuperVal" to "int",
            "myString" to "string",
            "a" to "boolean",
            "b" to "boolean",
            "hashMap" to "map|string|integer",
            "i" to "int",
            "f" to "int",
            "qwerty" to "qwerty"
        )
    }
}
