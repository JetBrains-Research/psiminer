package config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import storage.JsonASTStorage
import storage.Storage
import storage.paths.Code2SeqStorage
import java.io.File

@Serializable
abstract class StorageConfig {
    abstract fun createStorage(outputDirectory: File): Storage
}

@Serializable
@SerialName("JsonAST")
class JsonASTStorageConfig : StorageConfig() {
    override fun createStorage(outputDirectory: File): Storage = JsonASTStorage(outputDirectory)
}

@Serializable
@SerialName("Code2seq")
class Code2SeqStorageConfig(
    private val pathWidth: Int, // Maximum distance between two children of path LCA node
    private val pathLength: Int, // Maximum length of path
    private val maxPathsInTrain: Int? = null, // If not null then use only this number of paths to represent train trees
    private val maxPathsInTest: Int? = null, // If not null then use only this number of paths to represent val or test trees
    private val nodesToNumbers: Boolean = false // If true then each node type is replaced with number
) : StorageConfig() {
    override fun createStorage(outputDirectory: File): Storage = Code2SeqStorage(
        outputDirectory, pathWidth, pathLength, maxPathsInTrain, maxPathsInTest, nodesToNumbers
    )
}
