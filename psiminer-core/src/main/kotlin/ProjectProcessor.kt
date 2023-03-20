import astminer.storage.MetaDataStorage
import astminercompatibility.store
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import filter.Filter
import labelextractor.LabelExtractor
import psi.Parser
import psi.language.LanguageHandler
import psi.printTree
import psi.transformations.PsiTreeTransformation
import storage.Storage
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path

class ProjectProcessor(
    val languageHandler: LanguageHandler,
    val psiTreeTransformations: List<PsiTreeTransformation>,
    val filters: List<Filter>,
    val labelExtractor: LabelExtractor,
    val storage: Storage,
    val collectMetadata: Boolean = false,
) {

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

    fun processProject(
        project: Project,
        filesQueue: BlockingQueue<VirtualFile>,
        holdout: Dataset?,
        printTrees: Boolean
    ) {
        while (!filesQueue.isEmpty()) {
            val file = filesQueue.poll(POLL_TIMEOUT, TimeUnit.MILLISECONDS)
            if (file != null) {
                parser.parseFile(file, project) { processPsiTree(it, holdout, printTrees) }
            }
        }
    }

    fun closeMetadataStorage() {
        metaDataStorage?.close()
    }

    companion object {
        const val POLL_TIMEOUT = 5000L
    }
}
