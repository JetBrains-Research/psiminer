import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import psi.graphs.Edge
import psi.language.JavaHandler
import psi.language.KotlinHandler
import psi.language.LanguageHandler
import psi.language.PhpHandler
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BasePsiRequiredTest(private val psiSourceFile: File) : BasePlatformTestCase() {

    abstract val handler: LanguageHandler
    private val methods = mutableMapOf<String, PsiElement>()
    private class ResourceException(resourceRoot: String) : RuntimeException("Can't find resources in $resourceRoot")

    // We should define the root resources folder
    @Throws(ResourceException::class)
    override fun getTestDataPath(): String =
        BasePsiRequiredTest::class.java.getResource(resourcesRoot)?.path ?: throw ResourceException(resourcesRoot)

    @BeforeAll
    override fun setUp() {
        super.setUp()
        val methodsFile = File(testDataPath).resolve(psiSourceFile)
        myFixture.configureByFile(methodsFile.path).let {
            ReadAction.run<Exception> {
                handler
                    .splitByGranularity(it, GranularityLevel.Method)
                    .forEach {
                        val methodName = handler.methodProvider.getNameNode(it).text
                        methods[methodName] = it
                    }
            }
        }
    }

    @AfterAll
    override fun tearDown() {
        super.tearDown()
    }

    private class UnknownMethodException(methodName: String, fileName: String) :
        RuntimeException("Can't find method $methodName in $fileName")

    fun getMethod(methodName: String): PsiElement =
        methods[methodName] ?: throw UnknownMethodException(methodName, psiSourceFile.name)

    /**
     * Returns a Pair(lineNumber, columnNumber) where psiElement located in file.
     * Both lineNumber and columnNumber are 1-based.
     */
    private fun getPositionInFile(psiElement: PsiElement): Pair<Int, Int> {
        val document = getDocument(psiElement)
        val textOffset = psiElement.textOffset
        val lineNumber = document.getLineNumber(textOffset) // here 0-based
        val prevLineEndOffset = document.getLineEndOffset(lineNumber - 1)
        val columnNumber = textOffset - prevLineEndOffset
        return Pair(lineNumber + 1, columnNumber)
    }

    private fun getDocument(psiElement: PsiElement): Document {
        val containingFile = psiElement.containingFile
        val project = containingFile.project
        val psiDocumentManager = PsiDocumentManager.getInstance(project)
        return psiDocumentManager.getDocument(containingFile)
            ?: throw DocumentNotFoundException(psiSourceFile.name, psiElement.text)
    }

    private class DocumentNotFoundException(fileName: String, psiElementText: String) :
        RuntimeException("Can't find document for PsiElement $psiElementText in file $fileName")

    data class Vertex(val elementText: String, val positionInFile: Pair<Int, Int>)

    fun countOutgoingEdges(edges: List<Edge>): Map<Vertex, Int> =
        edges.groupBy { edge -> edge.from }
            .mapKeys { (psiElement, _) -> Vertex(psiElement.toString(), getPositionInFile(psiElement)) }
            .mapValues { (_, toVertices) -> toVertices.size }

    fun countIncomingEdges(edges: List<Edge>): Map<Vertex, Int> =
        edges.groupBy { edge -> edge.to }
            .mapKeys { (psiElement, _) -> Vertex(psiElement.toString(), getPositionInFile(psiElement)) }
            .mapValues { (_, fromVertices) -> fromVertices.size }

    data class CorrectNumberOfIncomingAndOutgoingEdges(
        val incoming: Map<String, Map<Vertex, Int>>,
        val outgoing: Map<String, Map<Vertex, Int>>
    )

    fun <K, V> Map<K, V>.containsAll(other: Map<K, V>?): Boolean {
        if (other == null) {
            return false
        }
        return this.entries.containsAll(other.entries)
    }

    companion object {
        // We cannot get the root of the class resources automatically.
        private const val resourcesRoot: String = "data"
    }
}

open class JavaPsiRequiredTest(source: String) : BasePsiRequiredTest(dataFolder.resolve("$source.$ext")) {
    override val handler: LanguageHandler = JavaHandler()

    companion object {
        val dataFolder = File("java")
        const val ext = "java"
    }
}

open class KotlinPsiRequiredTest(source: String) : BasePsiRequiredTest(dataFolder.resolve("$source.$ext")) {
    override val handler: LanguageHandler = KotlinHandler()

    companion object {
        val dataFolder = File("kotlin")
        const val ext = "kt"
    }
}

open class PhpPsiRequiredTest(source: String) : BasePsiRequiredTest(dataFolder.resolve("$source.$ext")) {
    override val handler: LanguageHandler = PhpHandler()

    companion object {
        val dataFolder = File("php")
        const val ext = "php"
    }
}
