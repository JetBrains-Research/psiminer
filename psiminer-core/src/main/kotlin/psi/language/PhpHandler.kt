package psi.language

import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import psi.method.PHPMethodProvider
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl
import psi.assignment.JavaAssignmentProvider
import psi.transformations.PhpTreeTransformation

class PhpHandler : LanguageHandler() {
    override val language = Language.PHP
    override val methodProvider = PHPMethodProvider()
    override val assignmentProvider = JavaAssignmentProvider()

    override val transformationType = PhpTreeTransformation::class.java
    override val classPsiType = PhpClass::class.java
    override val methodPsiType = MethodImpl::class.java

    override fun actionOnRecursiveCallIdentifier(root: PsiElement, action: (PsiElement) -> Unit) =
        PsiTreeUtil.collectElementsOfType(root, MethodReferenceImpl::class.java)
            .filter {
                val resolvedMethod = it.resolve()
                PsiManager.getInstance(root.project).areElementsEquivalent(root, resolvedMethod)
            }
            .forEach { callExpr ->
                // callExpr.canonicalText returns the name of the calling function
                PsiTreeUtil.collectElements(callExpr) { it.text == callExpr.canonicalText }.forEach(action)
            }
}
