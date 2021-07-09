package filter

import BasePsiRequiredTest
import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiMethod
import org.junit.Assert
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.treeSize

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TreeSizeFilterTest : BasePsiRequiredTest(getResourcesRootPath(TreeSizeFilterTest::class)) {

    private val javaMethods = mutableMapOf<String, PsiMethod>()

    @BeforeAll
    fun beforeAll() {
        javaMethods.putAll(getAllJavaMethods(javaMethodsFile, myFixture))
    }

    @ParameterizedTest
    @ValueSource(strings = [smallMethodName, largeMethodName])
    fun `test filtering by tree size`(methodName: String) = ReadAction.run<Exception> {
        // Since its unclear how to manually build PSI tree of the specific size the test is checking
        // filter that was initialized with size of small method would accept small method and decline large.
        val smallPsiTreeSize = javaMethods[smallMethodName]?.treeSize() ?:
            throw UnknownMethodException(smallMethodName, javaMethodsFile)
        val treeSizeFilter = TreeSizeFilter(maxSize = smallPsiTreeSize)
        val psiRoot = javaMethods[methodName] ?:
            throw UnknownMethodException(methodName, javaMethodsFile)
        Assert.assertEquals(methodName == smallMethodName, treeSizeFilter.validateTree(psiRoot, javaHandler))
    }

    companion object {
        const val javaMethodsFile = "JavaMethods.java"
        const val smallMethodName = "smallMethod"
        const val largeMethodName = "largeMethod"
    }
}