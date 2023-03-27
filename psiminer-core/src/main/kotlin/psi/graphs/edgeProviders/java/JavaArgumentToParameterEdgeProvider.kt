package psi.graphs.edgeProviders.java

import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import psi.graphs.*
import psi.graphs.edgeProviders.EdgeProvider

class JavaArgumentToParameterEdgeProvider : EdgeProvider(
    dependsOn = setOf(EdgeType.MethodDeclarationUsage),
    providedType = EdgeType.ArgumentToParameter,
) {
    override fun provideEdges(graph: CodeGraph): List<Edge> {
        val newEdges = mutableListOf<Edge>()
        graph.edges.withType(EdgeType.MethodDeclarationUsage).flatMap { (_, edges) -> edges.forward() }
            .forEach { (methodDeclaration, methodCall) ->
                if (methodDeclaration is PsiMethod && methodCall is PsiMethodCallExpression) {
                    val methodParameters = methodDeclaration.parameterList.parameters
                    val callArguments = methodCall.argumentList.expressions
//                    if (methodParameters.size != callArguments.size) {
//                        throw Exception("Parameter and argument lists have different length")
//                    }
                    callArguments.zip(methodParameters).forEach { (callArgument, methodParameter) ->
                        newEdges.add(Edge(callArgument, methodParameter, EdgeType.ArgumentToParameter))
                    }
                } else {
                    throw Exception("Invalid graph state")
                }
            }
        return newEdges
    }
}