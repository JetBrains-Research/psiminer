import astminer.common.getNormalizedToken
import astminer.common.model.Node
import astminer.common.normalizeToken
import astminer.common.preOrder
import astminer.parse.antlr.SimpleNode

object Config {
    const val storage = "code2seq"
    const val noTypes = false
    const val splitTypes = true
    const val resolvedTypesFirst = false
    const val hideMethodName = true

    const val nodesToNumbers = true

    const val maxPathWidth = 2
    const val maxPathHeight = 9

    val maxPathsInTrain: Int? = null
    val maxPathsInTest: Int? = null

    val maxTreeSize: Int? = null
}

object TreeConstants {
    const val RESOLVED_TYPE = "TOKEN_TYPE"
    const val NO_TYPE = "<NT>"

    const val methodNameToken = "<MN>"
    const val numberLiteralToken = "<NUM>"
    const val stringLiteralToken = "<STR>"
    const val booleanLiteralToken = "<BOOL>"
    const val defaultLiteralToken = "<LIT>"
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
            Dataset.Train -> {
                trainStatistic
            }
            Dataset.Val -> {
                valStatistic
            }
            Dataset.Test -> {
                testStatistic
            }
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
        print(" / ${root.getMetadata(TreeConstants.RESOLVED_TYPE)}")
    }
    print("\n")
    root.getChildren().forEach {
        printTree(it, withTypes, indent + indentStep, delimiter, indentStep)
    }
}

fun splitTypeToSubtypes(type: String): List<String> = type
        .split("[<>]".toRegex()).flatMap {
            it.trim()
                    .split("(?<=[a-z])(?=[A-Z])|_|[0-9]|(?<=[A-Z])(?=[A-Z][a-z])|\\s+".toRegex())
                    .map { s -> normalizeToken(s, "") }
                    .filter { it.isNotEmpty() }
                    .toList()
        }
