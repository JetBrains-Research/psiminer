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

enum class Language(val extensions: List<String>) {
    Java(listOf("java")),
    Kotlin(listOf("kt", "kts"))
}
