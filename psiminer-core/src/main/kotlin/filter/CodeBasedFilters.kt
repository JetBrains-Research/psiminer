package filter

import com.intellij.psi.PsiMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import psi.PsiNode

/***
 * Filter code snippets based on number of lines in it.
 * Corresponded code is normalized to contain only meaningful lines.
 * @param minCodeLines: Set the minimum number of lines in corresponded code snippet
 * @param maxCodeLines: Set the maximum number of lines in corresponded code snippet
 */
class CodeLinesFilter(private val minCodeLines: Int = 0, private val maxCodeLines: Int? = null) : Filter {
    override fun isGoodTree(root: PsiNode): Boolean {
        val psiNode = root.wrappedNode
        val cleanCodeLines = getCleanCode(
            if (psiNode is PsiMethod) psiNode.body?.text ?: "" else psiNode.text
        )
        return (minCodeLines <= cleanCodeLines.size) && (maxCodeLines == null || cleanCodeLines.size <= maxCodeLines)
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
