package psi

import astminer.common.NORMALIZED_TOKEN_KEY
import astminer.common.doTraversePreOrder
import astminer.common.model.Node
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

class PsiNode(
    val wrappedNode: PsiElement,
    private val parent: PsiNode?,
    val resolvedTokenType: String,
    private val presentableNodeType: String? = null
) : Node {

    private val metadata = HashMap<String, Any>()
    private val children = mutableListOf<PsiNode>()

    override fun getChildren(): List<PsiNode> = children.toList()

    override fun getMetadata(key: String): Any? = metadata[key]

    override fun getParent(): PsiNode? = parent

    override fun getToken(): String = wrappedNode.text

    fun getNormalizedToken(): String {
        val normalizedToken = metadata[NORMALIZED_TOKEN_KEY] as? String
        return if (normalizedToken == null || normalizedToken == "{}" || normalizedToken == "{|}") EMPTY_TOKEN
        else normalizedToken
    }

    override fun getTypeLabel(): String =
        presentableNodeType ?: wrappedNode.elementType.toString()

    override fun isLeaf(): Boolean = children.isEmpty()

    override fun removeChildrenOfType(typeLabel: String) {
        children.removeIf { it.getTypeLabel() == typeLabel }
    }

    override fun setMetadata(key: String, value: Any) {
        metadata.apply { put(key, value) }
    }

    fun setChildren(newChildren: List<PsiNode>) {
        children.clear()
        children.addAll(newChildren)
    }

    fun preOrder(): List<PsiNode> {
        val result = mutableListOf<Node>()
        doTraversePreOrder(this, result)
        return result.map { it as PsiNode }
    }

    companion object {
        const val EMPTY_TOKEN = "<EMPTY>"
    }
}
