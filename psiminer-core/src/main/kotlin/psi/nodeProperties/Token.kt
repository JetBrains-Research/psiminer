package psi.nodeProperties

import astminer.common.splitToSubtokens
import com.intellij.psi.PsiElement

val PsiElement.token: String?
    get() = when {
        children.isNotEmpty() -> null // if not leaf then there is no token
        TechnicalTokens.values().any { textMatches(it.presentableName) } -> text // if technical token then don't change
        else -> {
            val subtokens = splitToSubtokens(text)
            if (subtokens.isEmpty()) text // if not splittable then don't change (numbers, language symbols, etc)
            else subtokens.joinToString("|") // regular token returns as sequence of words
        }
    }

enum class TechnicalTokens(val presentableName: String) {
    METHOD_NAME("METHOD_NAME")
}
