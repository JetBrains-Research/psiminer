import astminer.common.model.*
import storage.XPathContext

object Config {
    const val psiTypeMetadataKey = "psiType"
    const val unknownType = "<UNKNOWN>"
}

enum class Dataset(val folderName: String) {
    Train("train"),
    Test("test"),
    Val("val")
}

fun toXPathContext(
    path: ASTPath,
    getToken: (Node) -> String = { node -> node.getToken()},
    getTokenType: (Node) -> String
): XPathContext {
    val startToken = getToken(path.upwardNodes.first())
    val endToken = getToken(path.downwardNodes.last())
    val startTokenType = getTokenType(path.upwardNodes.first())
    val endTokenType = getTokenType(path.downwardNodes.last())
    val astNodes = path.upwardNodes.map { OrientedNodeType(it.getTypeLabel(), Direction.UP) } +
            path.downwardNodes.map { OrientedNodeType(it.getTypeLabel(), Direction.DOWN) }
    return XPathContext(
        startTokenType = startTokenType,
        pathContext = PathContext(startToken, astNodes, endToken),
        endTokenType = endTokenType
    )
}
