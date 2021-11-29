package filter

import JavaPsiRequiredTest
import KotlinPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.Assert
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import psi.treeSize

/**
 * Since it is unclear how to manually build PSI tree of the specific size for the test,
 * filter initializes with size of small method.
 * It would accept small method and decline large method.
 */
interface TreeSizeFilterTest {
    val smallMethodName: String
        get() = "smallMethod"
    fun provideMethodNames(): Array<String> = arrayOf("smallMethod", "largeMethod")
}

class JavaTreeSizeFilterTest : TreeSizeFilterTest, JavaPsiRequiredTest("JavaMethods") {
    @ParameterizedTest
    @MethodSource("provideMethodNames")
    fun `test filtering java by tree size`(methodName: String) = ReadAction.run<Exception> {
        val smallPsiTreeSize = getMethod(smallMethodName).treeSize()
        val treeSizeFilter = TreeSizeFilter(maxSize = smallPsiTreeSize)
        val psiRoot = getMethod(methodName)
        Assert.assertEquals(methodName == smallMethodName, treeSizeFilter.validateTree(psiRoot, handler))
    }
}

class KotlinTreeSizeFilterTest : TreeSizeFilterTest, KotlinPsiRequiredTest("KotlinMethods") {
    @ParameterizedTest
    @MethodSource("provideMethodNames")
    fun `test filtering java by tree size`(methodName: String) = ReadAction.run<Exception> {
        val smallPsiTreeSize = getMethod(smallMethodName).treeSize()
        val treeSizeFilter = TreeSizeFilter(maxSize = smallPsiTreeSize)
        val psiRoot = getMethod(methodName)
        Assert.assertEquals(methodName == smallMethodName, treeSizeFilter.validateTree(psiRoot, handler))
    }
}
