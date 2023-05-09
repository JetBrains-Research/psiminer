package psi.graphs.graphMiners

import psi.graphs.EdgeType
import psi.graphs.edgeProviders.common.*
import psi.graphs.edgeProviders.ruby.RubyControlFlowEdgeProvider
import psi.graphs.edgeProviders.ruby.RubyDeclarationUsageEdgeProvider
import psi.language.JavaHandler

class RubyGraphMiner(
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
        EdgeType.DeclarationUsage to RubyDeclarationUsageEdgeProvider(),
        EdgeType.ControlFlow to RubyControlFlowEdgeProvider(),
        EdgeType.NextUsage to NextUsageEdgeProvider(),
        EdgeType.ComputedFrom to ComputedFromEdgeProvider(JavaHandler()),
        EdgeType.NextLexicalUsage to NextLexicalUsageEdgeProvider()
    )
)
