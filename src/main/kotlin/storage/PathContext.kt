package storage

import astminer.common.getNormalizedToken
import astminer.common.model.ASTPath
import astminer.common.model.Direction
import astminer.common.model.OrientedNodeType
import psi.PsiNode
import psi.PsiTypeResolver.Companion.NO_TYPE

class PathContext(
    val startType: String,
    val startToken: String,
    val nodePath: List<String>,
    val endToken: String,
    val endType: String
) {

    companion object {
        fun createFromASTPath(path: ASTPath): PathContext {
            val startNode = path.upwardNodes.first() as PsiNode
            val astNodes = path.upwardNodes.map { OrientedNodeType(it.getTypeLabel(), Direction.UP) } +
                    path.downwardNodes.map { OrientedNodeType(it.getTypeLabel(), Direction.DOWN) }
            val endNode = path.downwardNodes.last() as PsiNode
            return PathContext(
                startNode.resolvedTokenType,
                startNode.getNormalizedToken(),
                astNodes.map { it.typeLabel },
                endNode.getNormalizedToken(),
                endNode.resolvedTokenType
            )
        }

        // sortedBy is stable sort
        fun groupByResolvedTypes(paths: List<PathContext>): List<PathContext> =
            paths.sortedBy { path -> listOf(path.startType, path.endType).count { it == NO_TYPE } }
    }
}
