package storage.paths

import com.intellij.psi.PsiElement
import psi.PsiTypeResolver
import java.io.File

/***
 * Use extended path-based representation to represent each tree
 * More details about original format: https://github.com/tech-srl/code2seq
 * For each end of path token try to resolve its type
 * @param pathWidth: Maximum distance between two children of path LCA node
 * @param pathLength: Maximum length of path
 * @param splitTypes: If passed then split resolved types into subtypes
 * @param maxPathsInTrain: If not null then use only this number of paths to represent train trees
 * @param maxPathsInTest: If not null then use only this number of paths to represent val or test trees
 * @param nodesToNumbers: If true then each node type is replaced with number
 ***/
class TypedCode2SeqStorage(
    outputDirectory: File,
    pathWidth: Int,
    pathLength: Int,
    splitTypes: Boolean = true,
    maxPathsInTrain: Int? = null,
    maxPathsInTest: Int? = null,
    nodesToNumbers: Boolean = false
) : Code2SeqStorage(outputDirectory, pathWidth, pathLength, maxPathsInTrain, maxPathsInTest, nodesToNumbers) {

    private val typeResolver = PsiTypeResolver(splitTypes)

    override fun pathToString(path: List<PsiElement>): String {
        val vanillaPath = super.pathToString(path)
        val startTokenType = typeResolver.resolveType(path.first())
        val endTokenType = typeResolver.resolveType(path.last())
        return "$startTokenType,$vanillaPath,$endTokenType"
    }
}
