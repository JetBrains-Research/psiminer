package problem

import GranularityLevel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import psi.PsiNode

@Serializable
@SerialName("method name prediction")
class MethodNamePredictionConfig : ProblemConfig() {
    override fun createProblem(): Problem = MethodNamePrediction()
}

class MethodNamePrediction : Problem {

    override val granularityLevel = GranularityLevel.Method

    override fun processTree(root: PsiNode): LabeledTree? {
        val methodNameNode = root.getChildOfType(methodNameNodeType) as? PsiNode ?: return null
        val methodName = methodNameNode.getNormalizedToken()
        if (methodName == "") return null
        methodNameNode.setNormalizedToken(methodNameToken)
        root.preOrder().filter { it.getNormalizedToken() == methodName }
            .forEach { it.setNormalizedToken(selfCallToken) }
        return LabeledTree(root, methodName)
    }

    companion object {
        private const val methodNameToken = "<MN>"
        private const val selfCallToken = "<SELF>"

        private const val methodNameNodeType = "IDENTIFIER"
    }
}
