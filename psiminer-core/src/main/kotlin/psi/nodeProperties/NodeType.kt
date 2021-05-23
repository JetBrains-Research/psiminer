package psi.nodeProperties

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import kotlin.reflect.KProperty

var PsiElement.nodeType: String by NodeTypeDelegate()

class NodeTypeDelegate {
    private val modifiedType = HashMap<PsiElement, String>()

    operator fun getValue(thisRef: PsiElement, property: KProperty<*>): String =
        modifiedType[thisRef] ?: thisRef.elementType.toString()

    operator fun setValue(thisRef: PsiElement, property: KProperty<*>, newValue: String) {
        modifiedType[thisRef] = newValue
    }
}
