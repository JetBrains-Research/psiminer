package storage.paths

import astminer.common.model.Node
import com.intellij.psi.PsiElement
import psi.nodeProperties.isHidden
import psi.nodeProperties.nodeType
import psi.nodeProperties.token

class AstminerNodeWrapper(val psiNode: PsiElement, override val parent: Node? = null) : Node() {
    override val children: MutableList<AstminerNodeWrapper> by lazy {
        psiNode.children.filter { !it.isHidden }.map { AstminerNodeWrapper(it, this) }.toMutableList()
    }

    override val token: String = psiNode.token ?: ""

    override val typeLabel: String = psiNode.nodeType

    override fun removeChildrenOfType(typeLabel: String) {
        children.removeIf { it.typeLabel == typeLabel }
    }
}