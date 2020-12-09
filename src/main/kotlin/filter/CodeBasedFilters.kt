package filter

import com.intellij.psi.PsiMethod
import psi.PsiNode

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

class CodeLengthFilter(private val minCodeLength: Int = 0, private val maxCodeLength: Int? = null) : Filter {
    override fun isGoodTree(root: PsiNode): Boolean {
        val psiNode = root.wrappedNode
        val cleanCode = getCleanCode(
            if (psiNode is PsiMethod) psiNode.body?.text ?: "" else psiNode.text
        )
        return (minCodeLength <= cleanCode.size) && (maxCodeLength == null || cleanCode.size <= maxCodeLength)
    }
    companion object {
        const val name = "code length"
    }
}
