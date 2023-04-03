package psi.language

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import psi.transformations.GoTreeTransformation
import com.goide.psi.*
import psi.assignment.GoAssignmentProvider
import psi.method.GoMethodProvider

class GoHandler : LanguageHandler() {
    override val language = Language.GO
    override val methodProvider = GoMethodProvider()
    override val assignmentProvider = GoAssignmentProvider()

    override val transformationType = GoTreeTransformation::class.java
    override val classPsiType = GoTypeDeclaration::class.java // TODO: there are no classes in Go
    override val methodPsiType = GoFunctionOrMethodDeclaration::class.java

    override fun actionOnRecursiveCallIdentifier(root: PsiElement, action: (PsiElement) -> Unit) =
        PsiTreeUtil.collectElementsOfType(root, GoCallExpr::class.java)
            .filter {
                val resolvedMethod = it.expression.reference?.resolve()
                PsiManager.getInstance(root.project).areElementsEquivalent(root, resolvedMethod)
            }
            .forEach {
                it.expression.lastChild.apply(action)
            }
}
