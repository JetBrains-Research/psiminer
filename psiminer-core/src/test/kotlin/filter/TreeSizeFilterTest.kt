package filter

import BasePsiRequiredTest
import com.intellij.openapi.application.ReadAction
import org.junit.Assert
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.treeSize

internal class TreeSizeFilterTest : BasePsiRequiredTest() {

    @ParameterizedTest
    @ValueSource(strings = [smallMethodName, largeMethodName])
    fun `test filtering java by tree size`(methodName: String) = ReadAction.run<Exception> {
        // Since its unclear how to manually build PSI tree of the specific size the test is checking
        // filter that was initialized with size of small method would accept small method and decline large.
        val smallPsiTreeSize = getJavaMethod(smallMethodName).treeSize()
        val treeSizeFilter = TreeSizeFilter(maxSize = smallPsiTreeSize)
        val psiRoot = getJavaMethod(methodName)
        Assert.assertEquals(methodName == smallMethodName, treeSizeFilter.validateTree(psiRoot, javaHandler))
    }

    @ParameterizedTest
    @ValueSource(strings = [smallMethodName, largeMethodName])
    fun `test filtering kotlin by tree size`(methodName: String) = ReadAction.run<Exception> {
        // Since its unclear how to manually build PSI tree of the specific size the test is checking
        // filter that was initialized with size of small method would accept small method and decline large.
        val smallPsiTreeSize = getKotlinMethod(smallMethodName).treeSize()
        val treeSizeFilter = TreeSizeFilter(maxSize = smallPsiTreeSize)
        val psiRoot = getKotlinMethod(methodName)
        Assert.assertEquals(methodName == smallMethodName, treeSizeFilter.validateTree(psiRoot, kotlinHandler))
    }

    companion object {
        const val smallMethodName = "smallMethod"
        const val largeMethodName = "largeMethod"
    }
}
