import astminer.common.model.*

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

//interface XNode: Node {
//    fun getTokenType(): String
//}