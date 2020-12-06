import astminer.common.model.ASTPath

data class PathTypes(val startTokenType: String, val endTokenType: String)
fun getTypesFromASTPath(path: ASTPath): PathTypes {
    val startTokenType = path.upwardNodes.first()
                    .getMetadata(TreeConstants.resolvedType)?.toString() ?: TreeConstants.noType
    val endTokenType = path.downwardNodes.last()
                    .getMetadata(TreeConstants.resolvedType)?.toString() ?: TreeConstants.noType
    return PathTypes(startTokenType, endTokenType)
}

fun groupPathsByResolvedTypes(paths: List<ASTPath>, nPathContexts: Int?): List<ASTPath> {
    val twoResolvedTypes = mutableListOf<ASTPath>()
    val oneResolvedType = mutableListOf<ASTPath>()
    val zeroResolvedType = mutableListOf<ASTPath>()
    paths.forEach {
        val (startTokenType, endTokenType) = getTypesFromASTPath(it)
        var nResolvedTypes = 0
        if (startTokenType != TreeConstants.noType) ++nResolvedTypes
        if (endTokenType != TreeConstants.noType) ++nResolvedTypes
        when (nResolvedTypes) {
            2 -> twoResolvedTypes.add(it)
            1 -> oneResolvedType.add(it)
            0 -> zeroResolvedType.add(it)
        }
    }
    val orderedPaths = mutableListOf<ASTPath>()
    orderedPaths.addAll(twoResolvedTypes.let { it.take(nPathContexts ?: it.size) })
    orderedPaths.addAll(oneResolvedType.let { it.take(nPathContexts?.minus(orderedPaths.size) ?: it.size) })
    orderedPaths.addAll(zeroResolvedType.let { it.take(nPathContexts?.minus(orderedPaths.size) ?: it.size) })
    return orderedPaths
}
