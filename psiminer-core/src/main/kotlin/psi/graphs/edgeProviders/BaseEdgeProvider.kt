package psi.graphs.edgeProviders

import psi.graphs.CodeGraph
import psi.graphs.Edge
import psi.graphs.EdgeType

abstract class BaseEdgeProvider(val dependsOn: Set<EdgeType>, val providedType: EdgeType) {
    abstract fun provideEdges(graph: CodeGraph): List<Edge>
}
