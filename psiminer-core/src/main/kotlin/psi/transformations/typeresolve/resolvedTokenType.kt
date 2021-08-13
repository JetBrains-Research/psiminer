package psi.transformations.typeresolve

import astminer.common.normalizeToken
import com.intellij.psi.PsiElement
import psi.nodeProperties.PropertyDelegate
import psi.nodeProperties.registerPropertyDelegate
import kotlin.reflect.KProperty

const val KEYWORD = "<KWRD>"
const val OPERATOR = "<OP>"
const val NO_TYPE = "<NT>"

var PsiElement.resolvedTokenType: String? by TokenTypeDelegate().also { registerPropertyDelegate(it) }

class TokenTypeDelegate : PropertyDelegate<String>() {
    private var enable = false

    override operator fun getValue(thisRef: PsiElement, property: KProperty<*>): String? =
        if (!enable) null
        else {
            val tokenType = values[thisRef] ?: NO_TYPE
            if (tokenType in listOf(NO_TYPE, KEYWORD, OPERATOR)) tokenType
            else splitTypeToSubtypes(tokenType).joinToString("|")
        }

    override operator fun setValue(thisRef: PsiElement, property: KProperty<*>, value: String?) {
        super.setValue(thisRef, property, value)
        enable = true
    }

    private fun splitTypeToSubtypes(type: String): List<String> = type
        .split(genericRegex).flatMap {
            it.trim()
                .split(subtypesRegex)
                .map { s -> normalizeToken(s, "") }
                .filter { token -> token.isNotEmpty() }
                .toList()
        }

    companion object {
        private val genericRegex = "[<>]".toRegex()
        private val subtypesRegex = "(?<=[a-z])(?=[A-Z])|_|[0-9]|(?<=[A-Z])(?=[A-Z][a-z])|\\s+".toRegex()
    }
}
