import psi.language.JavaHandler
import psi.language.KotlinHandler
import psi.language.LanguageHandler

enum class GranularityLevel {
    File,
    Class,
    Method
}

enum class Dataset(val folderName: String) {
    Train("train"),
    Val("val"),
    Test("test")
}

enum class Language(val extensions: List<String>, val handler: LanguageHandler) {
    Java(listOf("java"), JavaHandler()),
    Kotlin(listOf("kt", "kts"), KotlinHandler())
}
