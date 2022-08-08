package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import psi.graphs.graphMiners.JavaGraphMiner
import storage.tree.JsonTreeStorage
import storage.Storage
import storage.graphs.JsonGraphStorage
import storage.paths.Code2SeqStorage
import java.io.File

@Serializable
abstract class StorageConfig {
    abstract fun createStorage(outputDirectory: File): Storage
}

@Serializable
@SerialName("json tree")
class JsonTreeStorageConfig : StorageConfig() {
    override fun createStorage(outputDirectory: File): Storage = JsonTreeStorage(outputDirectory)
}

@Serializable
@SerialName("code2seq")
class Code2SeqStorageConfig(
    private val pathWidth: Int, // Maximum distance between two children of path LCA node
    private val pathLength: Int, // Maximum length of path
    private val maxPathsInTrain: Int? = null, // If passed then use only this number of paths to represent train trees
    private val maxPathsInTest: Int? = null, // If passed then use only this number of paths to represent val/test trees
    private val nodesToNumbers: Boolean = false // If true then each node type is replaced with number
) : StorageConfig() {
    override fun createStorage(outputDirectory: File): Storage = Code2SeqStorage(
        outputDirectory, pathWidth, pathLength, maxPathsInTrain, maxPathsInTest, nodesToNumbers
    )
}

@Serializable
@SerialName("json graph")
class JsonGraphStorageConfig : StorageConfig() {
    override fun createStorage(outputDirectory: File): Storage = JsonGraphStorage(
        outputDirectory, JavaGraphMiner()
    )
}
