package mockedPsi

import com.intellij.psi.PsiElement
import io.mockk.every
import io.mockk.mockk

object SmallTree {
    /*
                        root
                  /       |      \
     interNodeLeft  interNodeMid  interNodeRight
            |          /     \
          leaf1      leaf2  leaf3
     */
    val root = mockk<PsiElement>()
    val interNodeLeft = mockk<PsiElement>()
    val interNodeMid = mockk<PsiElement>()
    val interNodeRight = mockk<PsiElement>()
    val leaf1 = mockk<PsiElement>()
    val leaf2 = mockk<PsiElement>()
    val leaf3 = mockk<PsiElement>()
    val allVertices = listOf(root, interNodeLeft, interNodeMid, interNodeRight, leaf1, leaf2, leaf3)

    init {
        every { root.children } returns arrayOf(interNodeLeft, interNodeMid, interNodeRight)
        every { interNodeLeft.children } returns arrayOf(leaf1)
        every { interNodeMid.children } returns arrayOf(leaf2, leaf3)
        every { interNodeRight.children } returns emptyArray()
        every { leaf1.children } returns emptyArray()
        every { leaf2.children } returns emptyArray()
        every { leaf3.children } returns emptyArray()
        allVertices.forEach { vertex ->
            mockPsiElementVisitor(vertex)
        }
    }
}
