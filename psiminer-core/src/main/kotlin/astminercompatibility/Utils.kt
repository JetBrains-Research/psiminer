package astminercompatibility

import AstminerNodeWrapper
import Dataset
import PATH_KEY
import astminer.common.model.DatasetHoldout
import astminer.common.model.LabeledResult
import astminer.common.model.NodeRange
import astminer.storage.MetaDataStorage
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import labelextractor.LabeledTree
import psi.getPosition

fun LabeledTree.toAstminerLabeledResult() = LabeledResult(
    root = AstminerNodeWrapper(root),
    label = label,
    filePath = root.getUserData(PATH_KEY)
        ?: throw IllegalStateException("Can't convert to labeled result: path is null")
)

fun MetaDataStorage.store(labeledTree: LabeledTree, holdout: Dataset?) {
    val astminerHoldout = when (holdout) {
        Dataset.Train -> DatasetHoldout.Train
        Dataset.Val -> DatasetHoldout.Validation
        Dataset.Test -> DatasetHoldout.Test
        null -> DatasetHoldout.None
    }
    store(labeledTree.toAstminerLabeledResult(), astminerHoldout)
}

fun PsiElement.nodeRange(document: Document): NodeRange {
    val textRange = textRange
    val startPosition = document.getPosition(textRange.startOffset)
    val endPosition = document.getPosition(textRange.endOffset)
    return NodeRange(startPosition, endPosition)
}

fun PsiElement.nodeRange(): NodeRange? {
    val document = PsiDocumentManager.getInstance(project).getDocument(containingFile) ?: return null
    return this.nodeRange(document)
}
