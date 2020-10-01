object Config {
    const val psiTypeMetadataKey = "psiType"
    const val unknownType = "<UNKNOWN>"

    const val storage = "code2seq"
    const val noTypes = true
    const val maxPathWidth = 2
    const val maxPathHeight = 9
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
