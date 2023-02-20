package psi.transformations.excludenode

import com.intellij.openapi.application.ReadAction
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import psi.nodeProperties.isHidden
import psi.transformations.PsiTreeTransformation

abstract class ExcludeNodeTransformation : PsiTreeTransformation {

    /***
     * Function to check whenever node should be ignored in PSI tree or not.
     * @param node: node to check
     * @return: `true` if node should be ignored
     */
    abstract fun isIgnored(node: PsiElement): Boolean

    override fun transform(root: PsiElement) = ReadAction.run<Exception> {
        PsiTreeUtil
            .collectElements(root) { isIgnored(it) }
            .forEach { it.isHidden = true }
    }
}
