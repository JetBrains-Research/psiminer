package psi.graphs

import BasePsiRequiredTest
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl
import org.junit.jupiter.api.TestInstance
import psi.language.LanguageHandler
import psi.language.PhpHandler
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseGraphTest(private val psiSourceFile: File) : BasePsiRequiredTest(psiSourceFile) {

    fun countOutgoingEdges(edges: List<Edge>): Map<Vertex, Int> =
        edges.groupBy { edge -> edge.from }
            .mapKeys { (psiElement, _) -> Vertex(psiElement.toString(), getPositionInFunction(psiElement)) }
            .mapValues { (_, toVertices) -> toVertices.size }

    fun countIncomingEdges(edges: List<Edge>): Map<Vertex, Int> =
        edges.groupBy { edge -> edge.to }
            .mapKeys { (psiElement, _) -> Vertex(psiElement.toString(), getPositionInFunction(psiElement)) }
            .mapValues { (_, fromVertices) -> fromVertices.size }

    fun <K, V> Map<K, V>.containsAll(other: Map<K, V>?): Boolean {
        if (other == null) {
            return false
        }
        return this.entries.containsAll(other.entries)
    }

    abstract fun PsiElement.methodRoot(): PsiElement

    private fun Document.getLineNumber(psiElement: PsiElement) =
        this.getLineNumber(psiElement.textOffset)

    /**
     * For PsiElement returns its position relatively to the function declaration.
     *
     * Function declaration corresponds to lineNumber=0.
     * Column number is also 0-based.
     */
    private fun getPositionInFunction(psiElement: PsiElement): Pair<Int, Int> {
        val document = getDocument(psiElement)
        val functionDeclarationLineNumber = document.getLineNumber(psiElement.methodRoot())
        val lineNumber = document.getLineNumber(psiElement) - functionDeclarationLineNumber
        val prevLineEndOffset = document.getLineEndOffset(lineNumber + functionDeclarationLineNumber - 1)
        val columnNumber = psiElement.textOffset - prevLineEndOffset - 1
        return Pair(lineNumber, columnNumber)
    }

    private fun getDocument(psiElement: PsiElement): Document {
        val containingFile = psiElement.containingFile
        val project = containingFile.project
        val psiDocumentManager = PsiDocumentManager.getInstance(project)
        return psiDocumentManager.getDocument(containingFile)
            ?: throw DocumentNotFoundException(psiSourceFile.name, psiElement.text)
    }

    data class Vertex(val elementText: String, val positionInFile: Pair<Int, Int>)

    data class CorrectNumberOfIncomingAndOutgoingEdges(
        val incoming: Map<String, Map<Vertex, Int>>,
        val outgoing: Map<String, Map<Vertex, Int>>
    )

    private class DocumentNotFoundException(fileName: String, psiElementText: String) :
        RuntimeException("Can't find document for PsiElement $psiElementText in file $fileName")

    class MethodRootNotFoundException(psiElementText: String) :
            RuntimeException("Can't find method root for $psiElementText")
}

open class PhpGraphTest(source: String) : BaseGraphTest(dataFolder.resolve("$source.$ext")) {

    override fun PsiElement.methodRoot(): PsiElement =
        when (this) {
            is MethodImpl -> this
            else -> this.parentOfType<MethodImpl>() as PsiElement? ?: throw MethodRootNotFoundException(this.text)
        }

    override val handler: LanguageHandler = PhpHandler()

    companion object {
        val dataFolder = File("php")
        const val ext = "php"
    }
}
