package psi.transformations.excludenode

import com.intellij.psi.PsiKeyword

class JavaExcludeKeywordTransformationTest : JavaExcludeTransformationTest() {
    override val transformation = ExcludeKeywordTransformation()
    override val excludeType = PsiKeyword::class
}