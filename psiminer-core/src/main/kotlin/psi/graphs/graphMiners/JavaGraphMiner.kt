package psi.graphs.graphMiners

import psi.graphs.EdgeType
import psi.graphs.edgeProviders.common.*
import psi.graphs.edgeProviders.java.JavaArgumentToParameterEdgeProvider
import psi.graphs.edgeProviders.java.JavaControlFlowEdgeProvider
import psi.graphs.edgeProviders.java.JavaDeclarationUsageEdgeProvider
import psi.graphs.edgeProviders.java.JavaMethodUsageEdgeProvider
import psi.language.JavaHandler

class JavaGraphMiner(
    edgeTypesToMine: Set<EdgeType> = setOf(
        EdgeType.Ast,
        EdgeType.NextToken,
        EdgeType.DeclarationUsage,
        EdgeType.ControlFlow,
        EdgeType.NextUsage,
        EdgeType.ComputedFrom,
        EdgeType.NextLexicalUsage,
        EdgeType.MethodDeclarationUsage,
        EdgeType.ArgumentToParameter,
    )
) : GraphMiner(
    edgeTypesToMine,
    mapOf(
        EdgeType.Ast to AstEdgeProvider(),
        EdgeType.NextToken to NextTokenEdgeProvider(),
        EdgeType.DeclarationUsage to JavaDeclarationUsageEdgeProvider(),
        EdgeType.ControlFlow to JavaControlFlowEdgeProvider(),
        EdgeType.NextUsage to NextUsageEdgeProvider(),
        EdgeType.ComputedFrom to ComputedFromEdgeProvider(JavaHandler()),
        EdgeType.NextLexicalUsage to NextLexicalUsageEdgeProvider(),
        EdgeType.MethodDeclarationUsage to JavaMethodUsageEdgeProvider(),
        EdgeType.ArgumentToParameter to JavaArgumentToParameterEdgeProvider(),
    )
)
