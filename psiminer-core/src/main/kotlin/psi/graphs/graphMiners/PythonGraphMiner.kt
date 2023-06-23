package psi.graphs.graphMiners

import psi.graphs.EdgeType
import psi.graphs.edgeProviders.common.*
import psi.graphs.edgeProviders.python.PythonCallDeclarationEdgeProvider
import psi.graphs.edgeProviders.python.PythonControlFlowEdgeProvider
import psi.graphs.edgeProviders.python.PythonDeclarationUsageEdgeProvider
import psi.language.PythonHandler

class PythonGraphMiner(
    edgeTypesToMine: Set<EdgeType> = setOf(
        EdgeType.Ast,
        EdgeType.NextToken,
        EdgeType.DeclarationUsage,
        EdgeType.ControlFlow,
        EdgeType.NextUsage,
        EdgeType.ComputedFrom,
        EdgeType.NextLexicalUsage,
        EdgeType.CallDeclaration
    )
) : GraphMiner(
    edgeTypesToMine,
    mapOf(
        EdgeType.Ast to AstEdgeProvider(),
        EdgeType.NextToken to NextTokenEdgeProvider(),
        EdgeType.DeclarationUsage to PythonDeclarationUsageEdgeProvider(),
        EdgeType.NextUsage to NextUsageEdgeProvider(),
        EdgeType.ComputedFrom to ComputedFromEdgeProvider(PythonHandler()),
        EdgeType.NextLexicalUsage to NextLexicalUsageEdgeProvider(),
        EdgeType.ControlFlow to PythonControlFlowEdgeProvider(),
        EdgeType.CallDeclaration to PythonCallDeclarationEdgeProvider()
    )
)
