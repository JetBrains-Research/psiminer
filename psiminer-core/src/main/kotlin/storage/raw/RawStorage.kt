package storage.raw

import Dataset
import com.intellij.psi.PsiElement
import labelextractor.LabeledTree
import storage.Storage
import java.io.File

class RawStorage(outputDirectory: File) : Storage(outputDirectory) {
    override val fileExtension: String = "raw"

//    private fun removeComments(root : PsiElement) : String {
//
//    }

    override fun convert(labeledTree: LabeledTree, holdout: Dataset?): String {
        println(labeledTree.label)
        return "${labeledTree.label} aaa"
    }
}