import astminer.storage.MetaDataStorage
import astminercompatibility.store
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import filter.Filter
import labelextractor.LabelExtractor
import psi.Parser
import psi.language.JavaHandler
import psi.language.KotlinHandler
import psi.language.PhpHandler
import psi.printTree
import psi.transformations.PsiTreeTransformation
import storage.Storage
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path

class RepositoryProcessor(
    val storage: Storage,
    collectMetadata: Boolean = false,
    val labelExtractor: LabelExtractor,
    val filters: List<Filter>,
    psiTreeTransformations: List<PsiTreeTransformation>,
    val language: Language,
    private val filesQueue: BlockingQueue<VirtualFile>,
    val project: Project,
    private val holdout: Dataset?,
    private val printTrees: Boolean
) : Runnable {
    private val languageHandler = when (language) {
        Language.Java -> JavaHandler()
        Language.Kotlin -> KotlinHandler()
        Language.PHP -> PhpHandler()
    }

    private val parser = Parser(languageHandler, psiTreeTransformations, labelExtractor.granularityLevel)

    private var metaDataStorage: MetaDataStorage? = null

    init {
        if (collectMetadata) {
            val metadataPath = Path(storage.outputDirectory.path, "metadata").toString()
            metaDataStorage = MetaDataStorage(metadataPath)
        }
    }

    private fun processPsiTree(psiRoot: PsiElement, holdout: Dataset? = null, printTrees: Boolean = false): Boolean {
        if (filters.any { !it.validateTree(psiRoot, languageHandler) }) return false
        val labeledTree = labelExtractor.extractLabel(psiRoot, languageHandler) ?: return false
        storage.store(labeledTree, holdout)
        metaDataStorage?.store(labeledTree, holdout)
        if (printTrees) labeledTree.root.printTree()
        return true
    }

    override fun run() {
        while (!filesQueue.isEmpty()) {
            val file = filesQueue.poll(5000, TimeUnit.MILLISECONDS)
            if (file != null)
                parser.parseFile(file, project) { processPsiTree(it, holdout, printTrees) }
        }
        metaDataStorage?.close()
        Thread.currentThread().interrupt()
    }
}
