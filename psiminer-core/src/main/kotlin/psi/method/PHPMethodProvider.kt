package psi.method

import com.intellij.psi.PsiElement

class PHPMethodProvider : MethodProvider() {
    override fun getNameNode(root: PsiElement): PsiElement {
        TODO("Not yet implemented")
    }

    override fun getBodyNode(root: PsiElement): PsiElement? {
        TODO("Not yet implemented")
    }

    override fun getDocComment(root: PsiElement): PsiElement? {
        TODO("Not yet implemented")
    }

    override fun getNonDocComments(root: PsiElement): Collection<PsiElement> {
        TODO("Not yet implemented")
    }

    override fun getDocCommentString(root: PsiElement): String {
        TODO("Not yet implemented")
    }

    override fun getNonDocCommentsString(root: PsiElement): String {
        TODO("Not yet implemented")
    }

    override fun isConstructor(root: PsiElement): Boolean {
        TODO("Not yet implemented")
    }
}