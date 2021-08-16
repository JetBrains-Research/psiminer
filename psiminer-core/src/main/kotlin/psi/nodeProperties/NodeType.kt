package psi.nodeProperties

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import kotlin.reflect.KProperty

var PsiElement.nodeType: String by NodeTypeDelegate().also { registerPropertyDelegate(it) }

class NodeTypeDelegate : PropertyDelegate<String>() {
    override operator fun getValue(thisRef: PsiElement, property: KProperty<*>): String =
        values[thisRef] ?: thisRef.elementType.toString()
}
