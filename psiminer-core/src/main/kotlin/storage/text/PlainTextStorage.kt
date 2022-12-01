package storage.text

import Dataset
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import labelextractor.LabeledTree
import psi.nodeProperties.isHidden
import psi.nodeProperties.technicalToken
import storage.Storage
import java.io.File

class PlainTextStorage(outputDirectory: File) : Storage(outputDirectory) {
    override val fileExtension: String = "jsonl"
    private val jsonSerializer = Json { encodeDefaults = false }

    @Serializable
    private data class PlainTextRepresentation(
        val label: String,
        val code: String
    )

    private fun processElement(element: PsiElement): String {
        return element.technicalToken ?: if (element is LeafPsiElement) {
            element.text
        } else {
            processMethodBody(element)
        }
    }

    private fun processMethodBody(root: PsiElement): String {
        return root.children.filter { !it.isHidden }.joinToString("") {
            processElement(it)
        }
    }

    override fun convert(labeledTree: LabeledTree, holdout: Dataset?): String {
        val sample = PlainTextRepresentation(labeledTree.label, processMethodBody(labeledTree.root))
        return jsonSerializer.encodeToString(sample)
    }
}
