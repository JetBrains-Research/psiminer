package psi.nodeProperties

import TECHNICAL_TOKEN_KEY
import astminer.common.splitToSubtokens
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

const val EMPTY_TOKEN = "EMPTY"
const val EMPTY_TOKEN_OLD = "<EMPTY>"

val PsiElement.token: String?
    get() = when {
        PsiTreeUtil.firstChild(this) != this -> EMPTY_TOKEN // if not leaf then there is no token
        technicalToken != null -> technicalToken
        else -> normalizedToken
    }

val PsiElement.tokenOldFormat: String?
    get() = when {
        PsiTreeUtil.firstChild(this) != this -> EMPTY_TOKEN_OLD // if not leaf then there is no token
        technicalToken != null -> technicalToken
        else -> text
    }

val PsiElement.normalizedToken: String
    get() = splitToSubtokens(text).let {
        if (it.isEmpty()) EMPTY_TOKEN
        else it.joinToString("|")
    }

var PsiElement.technicalToken: String?
    get() = getUserData(TECHNICAL_TOKEN_KEY)
    set(value) = putUserData(TECHNICAL_TOKEN_KEY, value)
