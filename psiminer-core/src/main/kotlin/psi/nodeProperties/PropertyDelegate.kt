package psi.nodeProperties

import com.intellij.psi.PsiElement
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class PropertyDelegate<T> : ReadWriteProperty<PsiElement, T?> {

    protected val values: HashMap<PsiElement, T> = hashMapOf()

    override operator fun getValue(thisRef: PsiElement, property: KProperty<*>): T? = values[thisRef]

    /**
     * Remember [value] for [thisRef] instance.
     */
    override operator fun setValue(thisRef: PsiElement, property: KProperty<*>, value: T?) {
        if (value != null) values[thisRef] = value
    }

    fun reset() = values.clear()
}

private val registeredPropertyDelegates = mutableListOf<PropertyDelegate<out Any>>()

fun registerPropertyDelegate(propertyDelegate: PropertyDelegate<out Any>) =
    registeredPropertyDelegates.add(propertyDelegate)

fun resetRegisteredPropertyDelegates() = registeredPropertyDelegates.forEach { it.reset() }
