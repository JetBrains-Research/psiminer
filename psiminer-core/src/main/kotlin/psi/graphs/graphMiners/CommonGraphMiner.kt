package psi.graphs.graphMiners

import psi.graphs.EdgeType
import psi.graphs.edgeProviders.common.AstEdgeProvider
import psi.graphs.edgeProviders.common.ControlFlowEdgeProvider
import psi.graphs.edgeProviders.common.DeclarationUsageEdgeProvider
import psi.graphs.edgeProviders.common.NextTokenEdgeProvider

class CommonGraphMiner(
    edgeTypesToMine: Set<EdgeType> = setOf(
        EdgeType.Ast,
//        EdgeType.NextToken,
//        EdgeType.DeclarationUsage,
        EdgeType.ControlFlow
    )
) : GraphMiner(
    edgeTypesToMine,
    mapOf(
        EdgeType.Ast to AstEdgeProvider(),
        EdgeType.NextToken to NextTokenEdgeProvider(),
        EdgeType.DeclarationUsage to DeclarationUsageEdgeProvider(),
        EdgeType.ControlFlow to ControlFlowEdgeProvider()
    )
)
