package psi.transformations.excludenode

import com.intellij.psi.PsiWhiteSpace

internal class JavaExcludeWhiteSpaceTransformationTest : JavaExcludeTransformationTest() {

    override val transformation = ExcludeWhiteSpaceTransformation()
    override val excludeType = PsiWhiteSpace::class
}


internal class KotlinExcludeWhiteSpaceTransformationTest : KotlinExcludeTransformationTest() {

    override val transformation = ExcludeWhiteSpaceTransformation()
    override val excludeType = PsiWhiteSpace::class
}