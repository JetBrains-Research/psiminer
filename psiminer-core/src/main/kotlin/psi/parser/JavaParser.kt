package psi.parser

import Language
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import psi.nodeIgnoreRules.PsiNodeIgnoreRule

class JavaParser(nodeIgnoreRules: List<PsiNodeIgnoreRule>) : Parser(nodeIgnoreRules) {

    override val extensions: List<String> = Language.Java.extensions

    override val psiElementVisitor: PsiElementVisitor = JavaNodeVisitor()

    private inner class JavaNodeVisitor : JavaRecursiveElementVisitor() {
        override fun visitElement(element: PsiElement) {
            super.visitElement(element)
            validateNode(element)
        }
    }
}
