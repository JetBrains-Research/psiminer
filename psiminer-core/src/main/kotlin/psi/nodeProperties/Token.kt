package psi.nodeProperties

import com.intellij.psi.PsiElement

val PsiElement.token: String?
    get() = if (children.isEmpty()) text else null
