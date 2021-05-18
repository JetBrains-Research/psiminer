package filter

import com.intellij.psi.PsiMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import psi.PsiNode

@Serializable
@SerialName("removeConstructor")
class ConstructorsFilterConfig : FilterConfig() {
    override fun createFilter(): Filter = ConstructorFilter()
}

class ConstructorFilter : Filter {
    override fun isGoodTree(root: PsiNode) = root.wrappedNode !is PsiMethod || !root.wrappedNode.isConstructor
}

@Serializable
@SerialName("removeAbstractMethod")
class AbstractMethodFilterConfig : FilterConfig() {
    override fun createFilter(): Filter = AbstractMethodFilter()
}

class AbstractMethodFilter : Filter {
    override fun isGoodTree(root: PsiNode): Boolean =
        root.wrappedNode !is PsiMethod || !root.wrappedNode.modifierList.hasModifierProperty("abstract")
}

@Serializable
@SerialName("removeOverrideMethod")
class OverrideMethodFilterConfig : FilterConfig() {
    override fun createFilter(): Filter = OverrideMethodFilter()
}

class OverrideMethodFilter : Filter {
    override fun isGoodTree(root: PsiNode): Boolean =
        root.wrappedNode !is PsiMethod || !root.wrappedNode.modifierList.hasAnnotation("Override")
}

@Serializable
@SerialName("removeEmptyMethod")
class EmptyMethodFilterConfig : FilterConfig() {
    override fun createFilter(): Filter = EmptyMethodFilter()
}

class EmptyMethodFilter : Filter {
    override fun isGoodTree(root: PsiNode): Boolean =
        root.wrappedNode !is PsiMethod || !(root.wrappedNode.body?.isEmpty ?: false)
}
