package config

import Language
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import psi.graphs.graphMiners.JavaGraphMiner
import psi.graphs.graphMiners.PhpGraphMiner
import psi.graphs.graphMiners.PythonGraphMiner
import storage.tree.JsonTreeStorage
import storage.Storage
import storage.graphs.JsonGraphStorage
import storage.paths.Code2SeqStorage
import storage.text.PlainTextStorage
import java.io.File

@Serializable
abstract class StorageConfig {
    open class UnsupportedStorageType(storageType: String, language: String) :
        IllegalArgumentException("$storageType storage doesn't support $language")

    abstract fun createStorage(outputDirectory: File, language: Language): Storage
}

@Serializable
@SerialName("json tree")
class JsonTreeStorageConfig : StorageConfig() {
    override fun createStorage(outputDirectory: File, language: Language): Storage = JsonTreeStorage(outputDirectory)
}

@Serializable
@SerialName("plain text")
class PlainTextStorageConfig : StorageConfig() {
    override fun createStorage(outputDirectory: File, language: Language): Storage = PlainTextStorage(outputDirectory)
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
    override fun createStorage(outputDirectory: File, language: Language): Storage = Code2SeqStorage(
        outputDirectory, pathWidth, pathLength, maxPathsInTrain, maxPathsInTest, nodesToNumbers
    )
}

@Serializable
@SerialName("json graph")
class JsonGraphStorageConfig : StorageConfig() {
    override fun createStorage(outputDirectory: File, language: Language): Storage =
        when (language) {
            Language.Java -> JsonGraphStorage(outputDirectory, JavaGraphMiner())
            Language.PHP -> JsonGraphStorage(outputDirectory, PhpGraphMiner())
            Language.Python -> JsonGraphStorage(outputDirectory, PythonGraphMiner())
            else -> throw UnsupportedStorageType("json graph", language.name)
        }
}
