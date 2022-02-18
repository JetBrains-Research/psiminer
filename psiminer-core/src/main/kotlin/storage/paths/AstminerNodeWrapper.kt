package storage.paths

import astminer.common.model.LabeledResult
import astminer.common.model.Node
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

fun LabeledTree.toAstminerLabeledResult(filePath: String) = LabeledResult(
    root = AstminerNodeWrapper(root),
    label = label,
    filePath = filePath
)