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

/*
Adapt from
https://github.com/tech-srl/code2seq/blob/master/JavaExtractor/JPredict/src/main/java/JavaExtractor/Visitors/FunctionVisitor.java#L52
 */
fun getCleanCode(code: String): List<String> {
    val cleanCode = code
        .replace("\r\n", "\n")
        .replace("\t", " ")
        .apply { if (startsWith("{\n")) substring(3).trim() }
        .apply { if (endsWith("\n}")) substring(0, length - 2).trim() }
    return cleanCode
        .split("\n")
        .map { it.trim() }
        .filter { it != "{" && it != "}" && it != "" && !it.startsWith("/") && !it.startsWith("*") }
}
