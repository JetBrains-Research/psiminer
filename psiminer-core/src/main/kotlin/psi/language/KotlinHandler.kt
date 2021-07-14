package psi.language

import Language
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import psi.method.KotlinMethodProvider
import psi.transformations.KotlinTreeTransformation

class KotlinHandler : LanguageHandler() {
    override val language = Language.Kotlin

    override val transformationType = KotlinTreeTransformation::class.java
    override val methodProvider = KotlinMethodProvider()

    override val classPsiType = KtClass::class.java
    override val methodPsiType = KtNamedFunction::class.java
}
