package filter

import com.intellij.psi.PsiMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import psi.PsiNode

@Serializable
@SerialName("CodeLines")
class CodeLinesFilterConfig(
    val minCodeLines: Int = 0, // Set the minimum number of lines in corresponded code snippet
    val maxCodeLines: Int? = null // Set the maximum number of lines in corresponded code snippet
) : FilterConfig() {
    override fun createFilter(): Filter = CodeLinesFilter(this)
}

class CodeLinesFilter(private val config: CodeLinesFilterConfig) : Filter {
    override fun isGoodTree(root: PsiNode): Boolean {
        val psiNode = root.wrappedNode
        val cleanCodeLines = getCleanCode(
            if (psiNode is PsiMethod) psiNode.body?.text ?: "" else psiNode.text
        )
        return (config.minCodeLines <= cleanCodeLines.size) &&
                (config.maxCodeLines == null || cleanCodeLines.size <= config.maxCodeLines)
    }

    /*
    Adapt from
    https://github.com/tech-srl/code2seq/blob/master/JavaExtractor/JPredict/src/main/java/JavaExtractor/Visitors/FunctionVisitor.java#L52
     */
    private fun getCleanCode(code: String): List<String> {
        val cleanCode = code
            .replace("\r\n", "\n")
            .replace("\t", " ")
            .apply { if (startsWith("{\n")) substring(3).trim() }
            .apply { if (endsWith("\n}")) substring(0, length - 2).trim() }
        return cleanCode.split("\n").map { it.trim() }
            .filter { it != "{" && it != "}" && it != "" && !it.startsWith("/") && !it.startsWith("*") }
    }
}
