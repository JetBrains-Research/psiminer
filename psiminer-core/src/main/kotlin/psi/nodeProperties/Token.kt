package psi.nodeProperties

import astminer.common.splitToSubtokens
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

const val EMPTY_TOKEN = "EMPTY"

val PsiElement.token: String?
    get() = when {
        PsiTreeUtil.firstChild(this) != this -> EMPTY_TOKEN // if not leaf then there is no token
        technicalToken != null -> technicalToken
        else -> normalizedToken
    }

val PsiElement.normalizedToken: String
    get() = splitToSubtokens(text).let {
        if (it.isEmpty()) EMPTY_TOKEN
        else it.joinToString("|")
    }

var PsiElement.technicalToken: String? by TechnicalTokenDelegate().also { registerPropertyDelegate(it) }

class TechnicalTokenDelegate : PropertyDelegate<String>()
