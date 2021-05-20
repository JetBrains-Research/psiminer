package psi.nodeProperties

import com.intellij.psi.PsiElement
import kotlin.reflect.KProperty

var PsiElement.isHidden: Boolean by HideElementDelegate()

class HideElementDelegate {
    private val hiddenElementStorage = HashMap<PsiElement, Boolean>()

    operator fun getValue(thisRef: PsiElement, property: KProperty<*>): Boolean = hiddenElementStorage[thisRef] ?: false

    operator fun setValue(thisRef: PsiElement, property: KProperty<*>, newValue: Boolean) {
        hiddenElementStorage[thisRef] = newValue
    }
}
