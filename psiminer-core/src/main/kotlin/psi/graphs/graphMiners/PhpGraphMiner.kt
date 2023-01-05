package psi.graphs.graphMiners

import psi.graphs.EdgeType
import psi.graphs.edgeProviders.common.*
import psi.graphs.edgeProviders.php.PhpControlFlowEdgeProvider
import psi.graphs.edgeProviders.php.PhpDeclarationUsageEdgeProvider
import psi.language.PhpHandler

class PhpGraphMiner(
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
        EdgeType.DeclarationUsage to PhpDeclarationUsageEdgeProvider(),
        EdgeType.NextUsage to NextUsageEdgeProvider(),
        EdgeType.ComputedFrom to ComputedFromEdgeProvider(PhpHandler()),
        EdgeType.NextLexicalUsage to NextLexicalUsageEdgeProvider(),
        EdgeType.ControlFlow to PhpControlFlowEdgeProvider()
    )
)
