package storage.graphs

import Dataset
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import labelextractor.LabeledTree
import psi.graphs.graphMiners.GraphMiner
import storage.Storage
import java.io.File

class JsonGraphStorage(outputDirectory: File, private val graphMiner: GraphMiner) : Storage(outputDirectory) {
    override val fileExtension = "json"

    private val jsonSerializer = Json { encodeDefaults = false }

    override fun convert(labeledTree: LabeledTree, holdout: Dataset?): String {
        val codeGraph = graphMiner.mine(labeledTree.root)
        val representation = JsonLabeledGraphRepresentation.convertLabeledCodeGraph(codeGraph, labeledTree.label)
        return jsonSerializer.encodeToString(representation)
    }
}
