package filter

import com.intellij.psi.PsiMethod
import psi.PsiNode

class ConstructorFilter : Filter {
    override fun isGoodTree(root: PsiNode) = root.wrappedNode !is PsiMethod || !root.wrappedNode.isConstructor
}

class AbstractMethodFilter : Filter {
    override fun isGoodTree(root: PsiNode): Boolean =
        root.wrappedNode !is PsiMethod || !root.wrappedNode.modifierList.hasModifierProperty("abstract")
}

class OverrideMethodFilter : Filter {
    override fun isGoodTree(root: PsiNode): Boolean =
        root.wrappedNode !is PsiMethod || !root.wrappedNode.modifierList.hasAnnotation("Override")
}

class EmptyMethodFilter : Filter {
    override fun isGoodTree(root: PsiNode): Boolean =
        root.wrappedNode !is PsiMethod || !(root.wrappedNode.body?.isEmpty ?: false)
}
