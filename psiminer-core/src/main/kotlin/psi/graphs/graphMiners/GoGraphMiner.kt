package psi.graphs.graphMiners

import psi.graphs.EdgeType
import psi.graphs.edgeProviders.common.*
import psi.graphs.edgeProviders.go.GoControlFlowEdgeProvider
import psi.graphs.edgeProviders.go.GoDeclarationUsageEdgeProvider
import psi.language.GoHandler

class GoGraphMiner(
    edgeTypesToMine: Set<EdgeType> = setOf(
        EdgeType.Ast,
        EdgeType.NextToken,
        EdgeType.DeclarationUsage,
        EdgeType.ControlFlow,
        EdgeType.NextUsage,
        EdgeType.ComputedFrom,
        EdgeType.NextLexicalUsage
    )
) : GraphMiner(
    edgeTypesToMine,
    mapOf(
        EdgeType.Ast to AstEdgeProvider(),
        EdgeType.NextToken to NextTokenEdgeProvider(),
        EdgeType.DeclarationUsage to GoDeclarationUsageEdgeProvider(),
        EdgeType.NextUsage to NextUsageEdgeProvider(),
        EdgeType.ComputedFrom to ComputedFromEdgeProvider(GoHandler()),
        EdgeType.NextLexicalUsage to NextLexicalUsageEdgeProvider(),
        EdgeType.ControlFlow to GoControlFlowEdgeProvider()
    )
)
