package problem

import GranularityLevel
import psi.PsiNode

class DocumentationPrediction : LabelExtractor {
    override val granularityLevel = GranularityLevel.Method

    override fun processTree(root: PsiNode): Sample? {
        return try {
            root.prettyPrint()
            val descriptionNodes = getDescriptionNodes(root)
            val tags = getTags(root)
            val javaDoc = getNormalizedJavaDoc(descriptionNodes, tags)
            removeJavaDoc(root)
            Sample(root, javaDoc)
        } catch (e: TypeCastException) {
            null
        }
    }

    private fun getDescriptionNodes(root: PsiNode): List<PsiNode> {
        return findInSubtreeWithType(root, docDescriptionNode)
    }

    private fun getTags(root: PsiNode): List<Pair<String, String>> {
        val docTags = findInSubtreeWithType(root, docTagNode)
        return docTags.map { tag ->
            val tagName = tag.getChildOfType(docTagNameNode) as PsiNode
            val tagValue = tag.getChildOfType(docParamTypeNode) as PsiNode
            return@map tagName.getNormalizedToken() to tagValue.getNormalizedToken()
        }
    }

    private fun findInSubtreeWithType(node: PsiNode, type: String): List<PsiNode> {
        return node.preOrder().filter { it.getTypeLabel() == type }
    }

    private fun getNormalizedJavaDoc(descriptionNodes: List<PsiNode>, tags: List<Pair<String, String>>): String {
        val normalizedDescription = descriptionNodes.joinToString("|") { it.getNormalizedToken() }
        val normalizedTags = tags.joinToString("|") { "(@${it.first},${it.second})" }
        return normalizedDescription + normalizedTags
    }


    private fun removeJavaDoc(root: PsiNode) {
        //TODO(Implement)
    }


    companion object {
        const val name = "documentation prediction"
        const val docDescriptionNode = "DOC_COMMENT_DATA"
        const val docTagNode = "DOC_TAG"
        const val docTagNameNode = "DOC_TAG_NAME"
        const val docParamTypeNode = "DOC_PARAMETER_REF|DOC_TAG_VALUE_TOKEN"
    }
}