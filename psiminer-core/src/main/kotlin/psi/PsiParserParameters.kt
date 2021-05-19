package psi

interface PsiParserParameters {
    val resolveTypes: Boolean
    val splitNames: Boolean
    val batchSize: Int
    val removeKeyword: Boolean
    val compressOperators: Boolean
    val removeComments: Boolean
    val removeJavaDoc: Boolean
    val compressTree: Boolean
}
