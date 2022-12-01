package mockedPsi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import io.mockk.every
import psi.nodeProperties.isHidden

fun mockPsiElementVisitor(vertex: PsiElement) {
    every { vertex.isHidden } returns false
    every { vertex.accept(any()) } answers {
        val visitor = args[0] as PsiElementVisitor
        visitor.visitElement(vertex)
    }
    every { vertex.acceptChildren(any()) } answers {
        val visitor = args[0] as PsiElementVisitor
        vertex.children.forEach {
            it.accept(visitor)
        }
    }
}
