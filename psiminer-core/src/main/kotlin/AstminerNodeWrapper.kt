import astminer.common.model.DatasetHoldout
import astminer.common.model.LabeledResult
import astminer.common.model.Node
import astminer.storage.MetaDataStorage
import com.intellij.psi.PsiElement
import labelextractor.LabeledTree
import psi.nodeProperties.isHidden
import psi.nodeProperties.nodeType
import psi.nodeProperties.token
import psi.nodeRange

class AstminerNodeWrapper(val psiNode: PsiElement, override val parent: Node? = null) : Node(psiNode.token) {
    override val children: MutableList<AstminerNodeWrapper> by lazy {
        psiNode.children.filter { !it.isHidden }.map { AstminerNodeWrapper(it, this) }.toMutableList()
    }

    override val range = psiNode.nodeRange()

    override val typeLabel: String = psiNode.nodeType

    override fun removeChildrenOfType(typeLabel: String) {
        children.removeIf { it.typeLabel == typeLabel }
    }
}

fun LabeledTree.toAstminerLabeledResult() = LabeledResult(
    root = AstminerNodeWrapper(root),
    label = label,
    filePath = root.getUserData(PATH_KEY)
        ?: throw IllegalStateException("Can't convert to labeled result: path is null")
)

fun MetaDataStorage.store(labeledTree: LabeledTree, holdout: Dataset?) {
    val astminerHoldout = when (holdout) {
        Dataset.Train -> DatasetHoldout.Train
        Dataset.Val -> DatasetHoldout.Validation
        Dataset.Test -> DatasetHoldout.Test
        null -> DatasetHoldout.None
    }
    store(labeledTree.toAstminerLabeledResult(), astminerHoldout)
}