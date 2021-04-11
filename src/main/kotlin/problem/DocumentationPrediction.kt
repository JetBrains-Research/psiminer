package problem

import GranularityLevel
import psi.PsiNode

class DocumentationPrediction : LabelExtractor {
    override val granularityLevel = GranularityLevel.Method

    override fun processTree(root: PsiNode): Sample? {
        try {
            val docCommentNode = getJavaDocNode(root) ?: return null
            val normalizedDoc = normalizeJavaDoc(docCommentNode)
            removeJavaDoc(root)
            return Sample(root, normalizedDoc)
        } catch (e: TypeCastException) {
            return null
        }
    }

    private fun getJavaDocNode(root: PsiNode): PsiNode? {
        return root.getChildOfType(javaDocNode) as? PsiNode
    }

    private fun normalizeJavaDoc(docNode: PsiNode): String {
        return docNode.getChildren().map { node ->
            when (node.getTypeLabel()) {
                docDescriptionNode -> node.getNormalizedToken()
                docTagNode -> normalizeTag(node)
                else -> ""
            }
        }.filter { it != "" }.joinToString("|")
    }

    private fun normalizeTag(tag: PsiNode): String {
        return tag.getChildren().map { node ->
            when (node.getTypeLabel()) {
                docTagNameNode -> "@${node.getNormalizedToken()}"
                docParamTypeNode, docDescriptionNode -> node.getNormalizedToken()
                else -> ""
            }
        }.filter { it != "" }.joinToString("|")
    }

    private fun removeJavaDoc(root: PsiNode) {
        val deleted = root.getChildren().filter { it.getTypeLabel().startsWith("DOC") }
        deleted.forEach { root.removeChildrenOfType(it.getTypeLabel()) }
    }

    companion object {
        const val name = "documentation prediction"
        const val javaDocNode = "DOC_COMMENT"
        const val docDescriptionNode = "DOC_COMMENT_DATA"
        const val docTagNode = "DOC_TAG"
        const val docTagNameNode = "DOC_TAG_NAME"
        const val docParamTypeNode = "DOC_PARAMETER_REF|DOC_TAG_VALUE_TOKEN"
    }
}
