package psi.graphs.graphMiners

import psi.graphs.EdgeType
import psi.graphs.edgeProviders.common.*
import psi.graphs.edgeProviders.java.JavaControlFlowEdgeProvider
import psi.graphs.edgeProviders.java.JavaDeclarationUsageEdgeProvider

class JavaGraphMiner(
    edgeTypesToMine: Set<EdgeType> = setOf(
        EdgeType.Ast,
        EdgeType.NextToken,
        EdgeType.DeclarationUsage,
        EdgeType.ControlFlow,
        EdgeType.NextUsage,
    )
) : GraphMiner(
    edgeTypesToMine,
    mapOf(
        EdgeType.Ast to AstEdgeProvider(),
        EdgeType.NextToken to NextTokenEdgeProvider(),
        EdgeType.DeclarationUsage to JavaDeclarationUsageEdgeProvider(),
        EdgeType.ControlFlow to JavaControlFlowEdgeProvider(),
        EdgeType.NextUsage to NextUsageEdgeProvider(),
    )
)
