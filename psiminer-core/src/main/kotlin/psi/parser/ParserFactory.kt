package psi.parser

import Language
import psi.nodeIgnoreRules.PsiNodeIgnoreRule

class ParserFactory(private val nodeIgnoreRules: List<PsiNodeIgnoreRule>) {

    fun createParser(language: Language): Parser =
        when (language) {
            Language.Java -> JavaParser(nodeIgnoreRules)
        }
}
