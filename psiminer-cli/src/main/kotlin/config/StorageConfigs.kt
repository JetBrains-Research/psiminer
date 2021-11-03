package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import storage.DatasetStorage
import storage.paths.Code2SeqDatasetStorage
import storage.tree.JsonTreeDatasetStorage
import storage.tree.JsonTreeV1DatasetStorage
import java.io.File

@Serializable
abstract class DatasetStorageConfig {
    abstract fun createDatasetStorage(outputDirectory: File): DatasetStorage
}

@Serializable
@SerialName("json tree")
class JsonTreeDatasetStorageConfig(
    private val withPaths: Boolean = false
) : DatasetStorageConfig() {
    override fun createDatasetStorage(outputDirectory: File): DatasetStorage =
        JsonTreeDatasetStorage(outputDirectory, withPaths)
}

@Serializable
@SerialName("json tree v1")
class JsonTreeDatasetStorageV1Config(
    private val withPaths: Boolean = false
) : DatasetStorageConfig() {
    override fun createDatasetStorage(outputDirectory: File): DatasetStorage =
        JsonTreeV1DatasetStorage(outputDirectory, withPaths)
}

@Serializable
@SerialName("code2seq")
class Code2SeqDatasetStorageConfig(
    private val pathWidth: Int, // Maximum distance between two children of path LCA node
    private val pathLength: Int, // Maximum length of path
    private val maxPathsInTrain: Int? = null, // If passed then use only this number of paths to represent train trees
    private val maxPathsInTest: Int? = null, // If passed then use only this number of paths to represent val/test trees
    private val nodesToNumbers: Boolean = false // If true then each node type is replaced with number
) : DatasetStorageConfig() {
    override fun createDatasetStorage(outputDirectory: File): DatasetStorage = Code2SeqDatasetStorage(
        outputDirectory, pathWidth, pathLength, maxPathsInTrain, maxPathsInTest, nodesToNumbers
    )
}
