package psi.nodeProperties

import com.intellij.psi.PsiElement
import kotlin.reflect.KProperty

var PsiElement.isHidden by HideElementDelegate().also { registerPropertyDelegate(it) }

class HideElementDelegate : PropertyDelegate<Boolean>() {
    override operator fun getValue(thisRef: PsiElement, property: KProperty<*>): Boolean =
        values[thisRef] ?: false
}
