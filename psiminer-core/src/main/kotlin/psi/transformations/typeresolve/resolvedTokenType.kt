package psi.transformations.typeresolve

import astminer.common.normalizeToken
import com.intellij.psi.PsiElement
import kotlin.reflect.KProperty

const val KEYWORD = "<KWRD>"
const val OPERATOR = "<OP>"
const val NO_TYPE = "<NT>"

var PsiElement.resolvedTokenType: String? by TokenTypeDelegate()

class TokenTypeDelegate {
    private val tokenType = HashMap<PsiElement, String>()

    private var enable = false

    operator fun getValue(thisRef: PsiElement, property: KProperty<*>): String? =
        if (!enable) null
        else {
            val tokenType = tokenType[thisRef] ?: NO_TYPE
            if (tokenType in listOf(NO_TYPE, KEYWORD, OPERATOR)) tokenType
            else splitTypeToSubtypes(tokenType).joinToString("|")
        }

    operator fun setValue(thisRef: PsiElement, property: KProperty<*>, newValue: String?) {
        enable = true
        if (newValue == null) throw IllegalArgumentException("try to set null to resolved token type")
        tokenType[thisRef] = newValue
    }

    private fun splitTypeToSubtypes(type: String): List<String> = type
        .split("[<>]".toRegex()).flatMap {
            it.trim()
                .split("(?<=[a-z])(?=[A-Z])|_|[0-9]|(?<=[A-Z])(?=[A-Z][a-z])|\\s+".toRegex())
                .map { s -> normalizeToken(s, "") }
                .filter { token -> token.isNotEmpty() }
                .toList()
        }
}
