package storage.raw

import Dataset
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import labelextractor.LabeledTree
import psi.nodeProperties.isHidden
import psi.nodeProperties.technicalToken
import storage.Storage
import java.io.File

class RawStorage(outputDirectory: File) : Storage(outputDirectory) {
    override val fileExtension: String = "txt"

    private fun processElement(element: PsiElement): String {
        if (element.technicalToken != null) return element.technicalToken!!
        if (element is LeafPsiElement) return element.text
        return processMethodBody(element)
    }

    private fun processMethodBody(root: PsiElement): String {
        return root.children.filter { !it.isHidden }.joinToString("") {
            processElement(it)
        }
    }

    override fun convert(labeledTree: LabeledTree, holdout: Dataset?): String {
        return "${labeledTree.label} '${processMethodBody(labeledTree.root).replace("\n", "\\n")}'"
    }
}