package mockedPsi

import com.intellij.psi.PsiElement
import io.mockk.every
import io.mockk.mockk

object TinyTree {
    /*
         root
       /      \
     leaf1  leaf2
     */
    val root = mockk<PsiElement>()
    val leaf1 = mockk<PsiElement>()
    val leaf2 = mockk<PsiElement>()
    val allVertices = listOf(root, leaf1, leaf2)

    init {
        every { root.children } returns arrayOf(leaf1, leaf2)
        every { leaf1.children } returns emptyArray()
        every { leaf2.children } returns emptyArray()
        allVertices.forEach { vertex ->
            mockPsiElementVisitor(vertex)
        }
    }
}
