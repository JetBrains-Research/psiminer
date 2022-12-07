package psi.language

import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import psi.assignment.PHPAssignmentProvider
import psi.method.PHPMethodProvider
import psi.transformations.JavaTreeTransformation
import com.jetbrains.php.lang.psi.*

class PHPHandler : LanguageHandler() {
    override val language = Language.PHP
    override val methodProvider = PHPMethodProvider()
    override val assignmentProvider = PHPAssignmentProvider()

    override val transformationType = JavaTreeTransformation::class.java
    override val classPsiType = com.intellij.psi. PhpCl  PhpClass:: PsiClass::class.java
    override val methodPsiType = PsiMethod::class.java

    override fun actionOnRecursiveCallIdentifier(root: PsiElement, action: (PsiElement) -> Unit) =
        PsiTreeUtil.collectElementsOfType(root, PsiMethodCallExpression::class.java)
            .filter {
                val resolvedMethod = it.resolveMethod()
                PsiManager.getInstance(root.project).areElementsEquivalent(root, resolvedMethod)
            }
            .forEach {
                // Last child in method call expression subtree corresponds to function identifier.
                it.methodExpression.lastChild.apply(action)
            }
}