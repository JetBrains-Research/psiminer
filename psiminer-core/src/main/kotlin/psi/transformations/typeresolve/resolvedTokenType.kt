package psi.transformations.typeresolve

import RESOLVED_TYPE_KEY
import astminer.common.normalizeToken
import com.intellij.psi.PsiElement

const val KEYWORD = "<KWRD>"
const val OPERATOR = "<OP>"
const val NO_TYPE = "<NT>"

private val genericRegex = "[<>]".toRegex()
private val subtypesRegex = "(?<=[a-z])(?=[A-Z])|_|[0-9]|(?<=[A-Z])(?=[A-Z][a-z])|\\s+".toRegex()

private var enable = false

var PsiElement.resolvedTokenType: String?
    get() {
        if (!enable) return null
        val tokenType = getUserData(RESOLVED_TYPE_KEY) ?: NO_TYPE
        return if (tokenType in listOf(NO_TYPE, KEYWORD, OPERATOR)) tokenType
        else splitTypeToSubtypes(tokenType).joinToString("|")
    }
    set(value) {
        putUserData(RESOLVED_TYPE_KEY, value)
        enable = true
    }

private fun splitTypeToSubtypes(type: String): List<String> =
    type.split(genericRegex).flatMap {
        it.trim()
            .split(subtypesRegex)
            .map { s -> normalizeToken(s, "") }
            .filter { token -> token.isNotEmpty() }
            .toList()
    }
