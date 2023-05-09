import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import java.io.File

val PATH_KEY = Key<String>("path")
val IS_HIDDEN_KEY = Key<Boolean>("hidden")
val NODE_TYPE_KEY = Key<String>("node type")
val TECHNICAL_TOKEN_KEY = Key<String>("tech token")
val RESOLVED_TYPE_KEY = Key<String>("resolved type")

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
    Kotlin(listOf("kt", "kts")),
    PHP(listOf("php")),
    Python(listOf("py")),
    Ruby(listOf("rb"))
}

/*
Adapt from
https://github.com/tech-srl/code2seq/blob/master/JavaExtractor/JPredict/src/main/java/JavaExtractor/Visitors/FunctionVisitor.java#L52
 */
fun getCleanCode(code: String): List<String> {
    val cleanCode = code
        .replace("\r\n", "\n")
        .replace("\t", " ")
        .replace("{", "")
        .replace("}", "")
    return cleanCode
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("/") && !it.startsWith("*") }
}

/**
 * Collect all files from project that correspond to given language
 * Search is based on checking extension of each file
 * @param project: project where run search
 * @return: list of all Virtual Files in project that correspond to required language.
 * @see VirtualFile
 */
fun extractProjectFiles(project: Project, language: Language): List<VirtualFile> {
    val virtualFileManager = VirtualFileManager.getInstance()
    return File(project.basePath ?: "")
        .walkTopDown()
        .filter { it.extension in language.extensions }
        .mapNotNull { virtualFileManager.findFileByNioPath(it.toPath()) }
        .toList()
}

class IncorrectPsiTypeException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    constructor(cause: Throwable) : this(null, cause)
}

class IncorrectMiningStateException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    constructor(cause: Throwable) : this(null, cause)
}
