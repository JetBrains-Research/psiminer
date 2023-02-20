package psi.transformations.removecomments

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.vfs.ReadonlyStatusHandler
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import org.jetbrains.kotlin.idea.util.application.executeCommand
import org.slf4j.LoggerFactory
import psi.transformations.PhpTreeTransformation

class PhpRemoveCommentsTransformation(private val removeDoc: Boolean) : PhpTreeTransformation {

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
