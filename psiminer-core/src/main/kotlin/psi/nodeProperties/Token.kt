package psi.nodeProperties

import astminer.common.splitToSubtokens
import com.intellij.psi.PsiElement
import kotlin.reflect.KProperty

val PsiElement.token: String?
    get() = when {
        children.isNotEmpty() -> null // if not leaf then there is no token
        technicalToken != null -> technicalToken
        normalizedToken != "" -> normalizedToken
        else -> text
    }

val PsiElement.normalizedToken: String
    get() = splitToSubtokens(text).joinToString("|")

var PsiElement.technicalToken: String? by TechnicalTokenDelegate()

class TechnicalTokenDelegate {
    private val technicalTokens = HashMap<PsiElement, String>()

    operator fun getValue(thisRef: PsiElement, property: KProperty<*>): String? = technicalTokens[thisRef]

    operator fun setValue(thisRef: PsiElement, property: KProperty<*>, newValue: String?) {
        if (newValue != null) technicalTokens[thisRef] = newValue
    }
}
