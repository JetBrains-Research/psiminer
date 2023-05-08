package psi.language

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyFunction
import psi.assignment.PythonAssignmentProvider
import psi.method.PythonMethodProvider
import psi.transformations.PythonTreeTransformation

class PythonHandler : LanguageHandler() {
    override val language = Language.Python
    override val methodProvider = PythonMethodProvider()
    override val assignmentProvider = PythonAssignmentProvider()

    override val transformationType = PythonTreeTransformation::class.java

    override val classPsiType = PyClass::class.java
    override val methodPsiType = PyFunction::class.java

    override fun actionOnRecursiveCallIdentifier(root: PsiElement, action: (PsiElement) -> Unit) =
        PsiTreeUtil.collectElementsOfType(root, PyCallExpression::class.java)
            .filter {
                val resolvedMethod = it.firstChild.reference?.resolve()
                PsiManager.getInstance(root.project).areElementsEquivalent(root, resolvedMethod)
            }
            .forEach {
                it.firstChild.firstChild.apply(action)
            }
}
