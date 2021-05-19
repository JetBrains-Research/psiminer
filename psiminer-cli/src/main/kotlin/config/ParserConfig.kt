package config

import kotlinx.serialization.Serializable
import psi.PsiParserParameters

@Serializable class ParserConfig : PsiParserParameters {
    override val resolveTypes: Boolean = true
    override val splitNames: Boolean = true
    override val batchSize: Int = 10_000
    override val removeKeyword: Boolean = false
    override val compressOperators: Boolean = false
    override val removeComments: Boolean = true
    override val removeJavaDoc: Boolean = true
    override val compressTree: Boolean = false
}
