package psi.nodeProperties

import TECHNICAL_TOKEN_KEY
import astminer.common.splitToSubtokens
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

const val EMPTY_TOKEN = "EMPTY"
const val EMPTY_TOKEN_V1 = "<EMPTY>"

val PsiElement.token: String?
    get() = when {
        PsiTreeUtil.firstChild(this) != this -> EMPTY_TOKEN // if not leaf then there is no token
        technicalToken != null -> technicalToken
        else -> normalizedToken
    }

val PsiElement.tokenV1: String
    get() = when {
        PsiTreeUtil.firstChild(this) != this -> EMPTY_TOKEN_V1 // if not leaf then there is no token
        else -> normalizedTokenV1
    }

val PsiElement.normalizedToken: String
    get() = splitToSubtokens(text).let {
        if (it.isEmpty()) EMPTY_TOKEN
        else it.joinToString("|")
    }

val PsiElement.normalizedTokenV1: String
    get() = text

var PsiElement.technicalToken: String?
    get() = getUserData(TECHNICAL_TOKEN_KEY)
    set(value) = putUserData(TECHNICAL_TOKEN_KEY, value)
