import astminer.common.getNormalizedToken
import astminer.common.model.Node
import astminer.common.preOrder
import astminer.parse.antlr.SimpleNode

object Config {
    const val storage = "code2seq"
    const val noTypes = true
    const val maxPathWidth = 2
    const val maxPathHeight = 9

    val maxPathsInTrain: Int? = null
    val maxPathsInTest: Int? = null

    val maxTreeSize: Int? = null
}

object TypeConstants {
    const val PSI_TYPE_METADATA_KEY = "PSI_TOKEN_TYPE"
    const val UNKNOWN_TYPE = "<UNKNOWN>"
    const val NO_TYPE = "<NULL>"
}

enum class Dataset(val folderName: String) {
    Train("train"),
    Val("val"),
    Test("test")
}

data class ExtractingStatistic(var nFiles: Int = 0, var nSamples: Int = 0, var nPaths: Int = 0) {
    override fun toString(): String =
            "#files: $nFiles, #samples: $nSamples, #paths: $nPaths (${nPaths.toDouble() / nSamples} paths per sample)"
}

data class DatasetStatistic(
    val trainStatistic: ExtractingStatistic = ExtractingStatistic(),
    val valStatistic: ExtractingStatistic = ExtractingStatistic(),
    val testStatistic: ExtractingStatistic = ExtractingStatistic()
) {
    override fun toString(): String =
            "Train holdout: $trainStatistic\n" +
            "Val holdout: $valStatistic\n" +
            "Test holdout: $testStatistic"

    fun addProjectStatistic(dataset: Dataset, extractingStatistic: ExtractingStatistic) {
        val currentStatistic = when (dataset) {
            Dataset.Train -> { trainStatistic }
            Dataset.Val -> { valStatistic }
            Dataset.Test -> { testStatistic }
        }
        currentStatistic.nFiles += extractingStatistic.nFiles
        currentStatistic.nSamples += extractingStatistic.nSamples
        currentStatistic.nPaths += extractingStatistic.nPaths
    }
}

fun getTreeSize(root: SimpleNode): Int = root.preOrder().size

fun printTree(root: Node, withTypes: Boolean, indent: Int = 0, delimiter: String = "--", indentStep: Int = 2) {
    print(delimiter.repeat(indent))
    print("${root.getTypeLabel()}: ${root.getNormalizedToken()}")
    if (withTypes) {
        print(" / ${root.getMetadata(TypeConstants.PSI_TYPE_METADATA_KEY)}")
    }
    print("\n")
    root.getChildren().forEach {
        printTree(it, withTypes, indent + indentStep, delimiter, indentStep)
    }
}

fun isNumber(token: String): Boolean = token.toIntOrNull() != null
