package psi

import astminer.common.doTraversePreOrder
import astminer.common.model.Node
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

class PsiNode(
    val wrappedNode: PsiElement,
    private var parent: PsiNode?,
    val resolvedTokenType: String,
    private val presentableNodeType: String? = null
) : Node {

    override val metadata = HashMap<String, Any>()
    private var children = mutableListOf<PsiNode>()

    override fun getChildren(): List<PsiNode> = children

    fun setChildren(newChildren: List<PsiNode>) {
        children = newChildren.toMutableList()
        children.forEach { it.setParent(this) }
    }

    private fun setParent(newParent: PsiNode?) {
        parent = newParent
    }

    override fun getParent(): PsiNode? = parent

    override fun getToken(): String = wrappedNode.text

    fun setNormalizedToken(normalizedToken: String) {
        metadata[NORMALIZED_TOKEN] = normalizedToken
    }

    fun getNormalizedToken(): String {
        val normalizedToken = metadata[NORMALIZED_TOKEN] as? String
        return if (normalizedToken == null || normalizedToken == "{}" || normalizedToken == "{|}") EMPTY_TOKEN
        else normalizedToken
    }

    override fun getTypeLabel(): String =
        presentableNodeType ?: wrappedNode.elementType.toString()

    override fun isLeaf(): Boolean = children.isEmpty()

    override fun removeChildrenOfType(typeLabel: String) {
        children.removeIf { it.getTypeLabel() == typeLabel }
    }

    fun preOrder(): List<PsiNode> {
        val result = mutableListOf<Node>()
        doTraversePreOrder(this, result)
        return result.map { it as PsiNode }
    }

    companion object {
        const val EMPTY_TOKEN = "<EMPTY>"
        const val NORMALIZED_TOKEN = "NORMALIZED_TOKEN"
    }
}
