package psi.graphs.graphMiners

import psi.graphs.EdgeType
import psi.graphs.edgeProviders.common.AstEdgeProvider
import psi.graphs.edgeProviders.common.NextTokenEdgeProvider

class CommonGraphMiner(
    edgeTypesToMine: Set<EdgeType> = setOf(EdgeType.Ast, EdgeType.NextToken)
) : GraphMiner(
    edgeTypesToMine,
    mapOf(EdgeType.Ast to AstEdgeProvider(), EdgeType.NextToken to NextTokenEdgeProvider())
)