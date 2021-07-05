package psi.transformations

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaToken
import com.intellij.psi.impl.source.tree.ElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import psi.nodeProperties.technicalToken

class JavaHideLiteralsTransformation(
    private val hideNumbers: Boolean = false,
    private val numberWhiteList: List<Int> = listOf(0, 1, 32, 64),
    private val hideStrings: Boolean = false
) : JavaTreeTransformation {
    override fun transform(root: PsiElement) =
        PsiTreeUtil.collectElementsOfType(root, PsiJavaToken::class.java)
            .forEach { token ->
                if (hideNumbers &&
                    numberLiterals.contains(token.elementType) &&
                    numberWhiteList.all { !token.textMatches(it.toString()) }) {
                    token.technicalToken = NUMBER_LITERAL
                }
                if (hideStrings && stringLiterals.contains(token.elementType)) {
                    token.technicalToken = STRING_LITERAL
                }
            }

    companion object {
        const val NUMBER_LITERAL = "<NUM>"
        val numberLiterals = TokenSet.orSet(ElementType.INTEGER_LITERALS, ElementType.REAL_LITERALS)

        const val STRING_LITERAL = "<STR>"
        val stringLiterals = TokenSet.orSet(ElementType.STRING_LITERALS, ElementType.TEXT_LITERALS)
    }
}
