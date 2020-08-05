object Config {
    const val psiTypeMetadataKey = "psiType"
    const val unknownType = "<UNKNOWN>"

    const val storage = "code2seq"
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
