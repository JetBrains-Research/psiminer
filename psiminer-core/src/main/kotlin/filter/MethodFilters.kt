package filter

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod

class ConstructorFilter : Filter {
    override fun isGoodTree(root: PsiElement) = root !is PsiMethod || !root.isConstructor
}

class AbstractMethodFilter : Filter {
    override fun isGoodTree(root: PsiElement): Boolean =
        root !is PsiMethod || !root.modifierList.hasModifierProperty("abstract")
}

class OverrideMethodFilter : Filter {
    override fun isGoodTree(root: PsiElement): Boolean =
        root !is PsiMethod || !root.modifierList.hasAnnotation("Override")
}

class EmptyMethodFilter : Filter {
    override fun isGoodTree(root: PsiElement): Boolean =
        root !is PsiMethod || !(root.body?.isEmpty ?: false)
}
