package config

import Language
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import psi.transformations.PsiTreeTransformation
import psi.transformations.excludenode.*

// ===== Common exclude node transformations =====

@Serializable
@SerialName("exclude whitespace")
class ExcludeWhiteSpaceTransformationConfig : PsiTreeTransformationConfig() {
    override fun createTreeTransformation(language: Language): PsiTreeTransformation = ExcludeWhiteSpaceTransformation()
}

@Serializable
@SerialName("exclude keywords")
class ExcludeKeywordTransformationConfig : PsiTreeTransformationConfig() {
    override fun createTreeTransformation(language: Language): PsiTreeTransformation =
        ExcludeKeywordTransformation()
}

@Serializable
@SerialName("exclude empty grammar lists")
class ExcludeEmptyGrammarListTransformationConfig : PsiTreeTransformationConfig() {
    override fun createTreeTransformation(language: Language): PsiTreeTransformation =
        ExcludeEmptyGrammarListsTransformation()
}

// ===== Language-specific exclude node transformations =====

@Serializable
@SerialName("exclude language symbols")
class ExcludeLanguageSymbolsTransformationConfig : PsiTreeTransformationConfig() {
    override fun createTreeTransformation(language: Language): PsiTreeTransformation =
        when (language) {
            Language.Java -> ExcludeJavaSymbolsTransformation()
            else -> throw UnsupportedLanguageTransformation("language symbols", language.name)
        }
}

@Serializable
@SerialName("exclude package")
class ExcludePackageStatementTransformationConfig : PsiTreeTransformationConfig() {
    override fun createTreeTransformation(language: Language): PsiTreeTransformation =
        when (language) {
            Language.Java -> ExcludePackageStatementTransformation()
            else -> throw UnsupportedLanguageTransformation("package", language.name)
        }
}

@Serializable
@SerialName("exclude imports")
class ExcludeImportStatementsTransformationConfig : PsiTreeTransformationConfig() {
    override fun createTreeTransformation(language: Language): PsiTreeTransformation =
        when (language) {
            Language.Java -> ExcludeImportStatementsTransformation()
            else -> throw UnsupportedLanguageTransformation("imports", language.name)
        }
}
