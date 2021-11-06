package psi.nodeProperties

import IS_HIDDEN_KEY
import com.intellij.psi.PsiElement

var PsiElement.isHidden
    get() = getUserData(IS_HIDDEN_KEY) ?: false
    set(value) = putUserData(IS_HIDDEN_KEY, value)
