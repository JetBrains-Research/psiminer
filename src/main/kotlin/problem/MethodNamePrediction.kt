package problem

import Dataset
import GranularityLevel
import astminer.common.getNormalizedToken
import astminer.common.preOrder
import astminer.common.setNormalizedToken
import astminer.parse.antlr.SimpleNode
import storage.Storage

class MethodNamePrediction(private val storage: Storage) : Problem {

    override val granularityLevel = GranularityLevel.Method

    override fun processTree(root: SimpleNode, holdout: Dataset) {
        val methodNameNode = root.getChildOfType(methodNameNodeType) as? SimpleNode ?: return
        val methodName = methodNameNode.getNormalizedToken()
        if (methodName == "") return
        methodNameNode.setNormalizedToken(methodNameToken)

        root.preOrder().filter { it.getNormalizedToken() == methodName }.forEach {
            it.setNormalizedToken(selfCallToken)
        }

        storage.store(root, methodName, holdout)
    }

    companion object {
        const val name: String = "method name prediction"

        private const val methodNameToken = "<MN>"
        private const val selfCallToken = "<SELF>"

        private const val methodNameNodeType = "IDENTIFIER"
    }
}
