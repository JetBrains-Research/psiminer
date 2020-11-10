import astminer.common.model.ASTPath

data class PathTypes(val startTokenType: String, val endTokenType: String)
fun getTypesFromASTPath(path: ASTPath): PathTypes {
    val startTokenType = path.upwardNodes.first()
                    .getMetadata(TypeConstants.PSI_TYPE_METADATA_KEY)?.toString() ?: TypeConstants.NO_TYPE
    val endTokenType = path.downwardNodes.last()
                    .getMetadata(TypeConstants.PSI_TYPE_METADATA_KEY)?.toString() ?: TypeConstants.NO_TYPE
    return PathTypes(startTokenType, endTokenType)
}

fun groupPathsByResolvedTypes(paths: List<ASTPath>, nPathContexts: Int?): List<ASTPath> {
    val twoResolvedTypes = mutableListOf<ASTPath>()
    val oneResolvedType = mutableListOf<ASTPath>()
    val zeroResolvedType = mutableListOf<ASTPath>()
    paths.forEach {
        val (startTokenType, endTokenType) = getTypesFromASTPath(it)
        var nResolvedTypes = 0
        if (startTokenType !in TypeConstants.unresolvedTypes) ++nResolvedTypes
        if (endTokenType !in TypeConstants.unresolvedTypes) ++nResolvedTypes
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
