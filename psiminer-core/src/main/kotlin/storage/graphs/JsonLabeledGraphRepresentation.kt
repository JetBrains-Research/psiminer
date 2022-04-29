package storage.graphs

import com.intellij.psi.PsiElement
import kotlinx.serialization.Serializable
import psi.graphs.CodeGraph

object JsonLabeledGraphRepresentation {

    @Serializable
    data class LabeledGraphRepresentation(
        val label: String,
        val graph: JsonGraphRepresentation.GraphRepresentation
    )

    fun convertLabeledCodeGraph(codeGraph: CodeGraph, label: String, nodeTypeMapper: (PsiElement) -> Long) =
        LabeledGraphRepresentation(
            label,
            JsonGraphRepresentation.convertCodeGraph(codeGraph, nodeTypeMapper)
        )
}
