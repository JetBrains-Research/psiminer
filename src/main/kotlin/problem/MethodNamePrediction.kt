package problem

import GranularityLevel
import astminer.common.setNormalizedToken
import psi.PsiNode

class MethodNamePrediction : LabelExtractor {

    override val granularityLevel = GranularityLevel.Method

    override fun processTree(root: PsiNode): Sample? {
        val methodNameNode = getMethodNameNode(root) ?: return null
        val methodName = methodNameNode.getNormalizedToken()
        if (methodName.isBlank()) return null
        replaceMethodNameWithKeyword(methodNameNode)
        normalizeRecursionCalls(root, methodName)
        return Sample(root, methodName)
    }

    private fun getMethodNameNode(root: PsiNode): PsiNode? {
        return root.getChildOfType(methodNameNodeType) as? PsiNode
    }

    private fun replaceMethodNameWithKeyword(methodNameNode: PsiNode){
        methodNameNode.setNormalizedToken(methodNameToken)
    }

    private fun normalizeRecursionCalls(root: PsiNode, methodName: String) {
        root.preOrder().filter { it.getNormalizedToken() == methodName }
            .forEach { it.setNormalizedToken(selfCallToken) }
    }

    companion object {
        const val name: String = "method name prediction"

        private const val methodNameToken = "<MN>"
        private const val selfCallToken = "<SELF>"

        private const val methodNameNodeType = "IDENTIFIER"
    }
}
