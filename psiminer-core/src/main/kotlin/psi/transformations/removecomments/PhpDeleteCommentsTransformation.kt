package psi.transformations.removecomments

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.slf4j.LoggerFactory
import psi.transformations.PhpTreeTransformation

class PhpHideCommentsTransformation(private val removeDoc: Boolean) : PhpTreeTransformation {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun transform(root: PsiElement) {
        logger.warn("TROLOLOLOLOLO")
//        val project  = root.project
//        ApplicationManager.getApplication().invokeAndWait {
//            WriteCommandAction.runWriteCommandAction(project) {
//                PsiTreeUtil.collectElementsOfType(root, PsiComment::class.java)
//                    .forEach { it.delete() }
//            }
//        }
    }

    fun collectComments(root: PsiElement): Set<PsiElement> {
        return PsiTreeUtil.collectElementsOfType(root, PsiComment::class.java).toSet()
    }

    private fun removeComment(comment: PsiElement) {
        comment.delete()
    }
}
