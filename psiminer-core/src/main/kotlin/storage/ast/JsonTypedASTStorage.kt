package storage.ast

import com.intellij.psi.PsiElement
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import problem.LabeledTree
import psi.PsiTypeResolver
import psi.nodeProperties.nodeType
import psi.nodeProperties.token
import storage.Storage
import java.io.File

class JsonTypedASTStorage(
    outputDirectory: File,
    splitTypes: Boolean
) : Storage(outputDirectory) {

    override val fileExtension: String = "jsonl"
    private val typeResolver = PsiTypeResolver(splitTypes)

    @Serializable
    private data class NodeRepresentation(
        val token: String?,
        val tokenType: String?,
        val nodeType: String,
        val children: List<Int>,
    )
    @Serializable
    private data class TreeRepresentation(val label: String, val nodes: List<NodeRepresentation>)

    private fun collectNodeRepresentation(root: PsiElement): List<NodeRepresentation> {
        val numerateTreeVisitor = NumerateTreeVisitor()
        root.accept(numerateTreeVisitor)
        return numerateTreeVisitor.orderTree().map {
            val childrenIds = it.children.mapNotNull { child -> numerateTreeVisitor.nodeToId[child] }
            val tokenType = typeResolver.resolveType(it)
            NodeRepresentation(it.token, tokenType, it.nodeType, childrenIds)
        }
    }

    override fun convert(labeledTree: LabeledTree, outputDirection: OutputDirection): String {
        val nodesRepresentation = collectNodeRepresentation(labeledTree.root)
        val treeRepresentation = TreeRepresentation(labeledTree.label, nodesRepresentation)
        return Json.encodeToString(treeRepresentation)
    }
}