package psi.transformations.excludenode

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import psi.transformations.PhpTreeTransformation

class ExcludePhpSymbolsTransformation : PhpTreeTransformation, ExcludeNodeTransformation() {
    private val skipElementTypes = listOf("(", ")", "{", "}", "arrow", "semicolon", "comma", "dot")

    override fun isIgnored(node: PsiElement): Boolean {
        return node.elementType.toString() in skipElementTypes
    }
}
