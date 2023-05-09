package psi.language

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.methodCall.RCallImpl
import psi.assignment.RubyAssignmentProvider
import psi.method.RubyMethodProvider
import psi.transformations.RubyTreeTransformation

class RubyHandler : LanguageHandler() {
    override val language = Language.Ruby
    override val methodProvider = RubyMethodProvider()
    override val assignmentProvider = RubyAssignmentProvider()

    override val transformationType = RubyTreeTransformation::class.java

    override val classPsiType = RClass::class.java
    override val methodPsiType = RMethod::class.java

    override fun actionOnRecursiveCallIdentifier(root: PsiElement, action: (PsiElement) -> Unit) =
        PsiTreeUtil.collectElementsOfType(root, RCallImpl::class.java)
            .filter {
                val resolvedMethod = it.firstChild.reference?.resolve()
                PsiManager.getInstance(root.project).areElementsEquivalent(root, resolvedMethod)
            }
            .forEach {
                it.firstChild.firstChild.apply(action)
            }
}
