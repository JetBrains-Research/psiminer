import astminer.common.model.ASTPath
import astminer.common.model.Direction
import astminer.common.model.Node
import astminer.common.model.OrientedNodeType
import astminer.common.model.PathContext
import storage.XPathContext

object Config {
    const val psiTypeMetadataKey = "psiType"
    const val unknownType = "<UNKNOWN>"

    const val maxPathWidth = 2
    const val maxPathHeight = 8
}

enum class Dataset(val folderName: String) {
    Train("train"),
    Val("val"),
    Test("test")
}

data class HoldoutStatistic(var nFiles: Int = 0, var nPaths: Int = 0) {
    override fun toString(): String = "#files: $nFiles, #paths: $nPaths"
}

data class DatasetStatistic(
    val trainStatistic: HoldoutStatistic = HoldoutStatistic(),
    val valStatistic: HoldoutStatistic = HoldoutStatistic(),
    val testStatistic: HoldoutStatistic = HoldoutStatistic()
) {
    override fun toString(): String = "Train holdout: $trainStatistic\n" +
            "Val holdout: $valStatistic\n" +
            "Test holdout: $testStatistic"

    fun addFileStatistic(dataset: Dataset, nPaths: Int) {
        when (dataset) {
            Dataset.Train -> {
                trainStatistic.nFiles += 1
                trainStatistic.nPaths += nPaths
            }
            Dataset.Val -> {
                valStatistic.nFiles += 1
                valStatistic.nPaths += nPaths
            }
            Dataset.Test -> {
                testStatistic.nFiles += 1
                testStatistic.nPaths += nPaths
            }
        }
    }
}

fun toXPathContext(
    path: ASTPath,
    getToken: (Node) -> String = { node -> node.getToken() },
    getTokenType: (Node) -> String
): XPathContext {
    val startToken = getToken(path.upwardNodes.first())
    val endToken = getToken(path.downwardNodes.last())
    val startTokenType = getTokenType(path.upwardNodes.first())
    val endTokenType = getTokenType(path.downwardNodes.last())
    val astNodes = path.upwardNodes.map { OrientedNodeType(it.getTypeLabel(), Direction.UP) } +
            path.downwardNodes.map { OrientedNodeType(it.getTypeLabel(), Direction.DOWN) }
    return XPathContext(
        startTokenType = startTokenType,
        pathContext = PathContext(startToken, astNodes, endToken),
        endTokenType = endTokenType
    )
}
