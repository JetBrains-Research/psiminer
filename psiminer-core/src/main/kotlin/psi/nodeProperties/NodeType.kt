package psi.nodeProperties

import NODE_TYPE_KEY
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

var PsiElement.nodeType: String
    get() = getUserData(NODE_TYPE_KEY) ?: elementType.toString()
    set(value) = putUserData(NODE_TYPE_KEY, value)
