import astminer.common.getNormalizedToken
import astminer.common.model.*
import astminer.common.preOrder
import astminer.common.setNormalizedToken
import astminer.common.splitToSubtokens
import astminer.parse.antlr.SimpleNode
import astminer.parse.antlr.compressTree
import astminer.parse.antlr.decompressTypeLabel
import astminer.parse.antlr.java.JavaMethodSplitter
import astminer.parse.antlr.java.JavaParser
import astminer.paths.PathMiner
import astminer.paths.PathRetrievalSettings
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.*
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.util.elementType
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

val node2type: MutableMap<Node, String> = hashMapOf()

//val mapper = ObjectMapper().registerModule(KotlinModule())

class Runner : ApplicationStarter {

    override fun getCommandName(): String = "code2vec"

    override fun main(args: Array<out String>) {
        println("code2vec plugin started ;)")

        // System.setProperty("org.litote.mongo.test.mapping.service", "org.litote.kmongo.jackson.JacksonClassMappingTypeService")

        if (args.size != 3) {
            println("incorrect number of arguments!")
            exitProcess(0)
        }

        val projectPath = args[1] // "./c2v_data/java-small"
        val outputPath = args[2] // "./c2v_data_preprocessed/java-small"

        val project = ProjectUtil.openOrImport(projectPath, null, true)
        if (project == null) {
            println("Could not load project from $projectPath")
        } else {
            println("opened project")
            println("processing files...")
            val miner = PathMiner(PathRetrievalSettings(5, 5))
            val storage = XCode2VecPathStorage("astminer_withTypes", "$outputPath/withTypes")

            val minerGold = PathMiner(PathRetrievalSettings(5, 5))
            val storageGold = XCode2VecPathStorage("astminer_gold", "$outputPath/gold")

            // val LIMIT = 1800
            val executionTime = measureTimeMillis {
                ProjectRootManager.getInstance(project).contentRoots.forEach { root ->
                    var total = 0L
                    VfsUtilCore.iterateChildrenRecursively(root, null) { total += 1; true }

                    var index = 0L
                    VfsUtilCore.iterateChildrenRecursively(root, null) { vFile ->
                        val psi = PsiManager.getInstance(project).findFile(vFile)
                        index += 1
                        //if (index > LIMIT) {
                        //    println("[LIMIT REACHED] terminating")
                        //    return@iterateChildrenRecursively false
                        //}
                        println("processing ${vFile.canonicalPath} $index / $total")

                        // verify path
                        val chunks = vFile.canonicalPath?.split("/")
                            ?.drop(4)
                            ?.joinToString("/", prefix = "/")
                            ?: ""
                        val datasetName = "/java-med"
                        if (!chunks.startsWith(datasetName)) {
                            println("skipping! chunks: $chunks")
                            return@iterateChildrenRecursively true
                        } else {
                            println("👍🏻 chunks: $chunks")
                        }

                        // determine if it's train / val / test
                        // val dataset = Dataset.valueOf()
                        val dataset = if (chunks.startsWith("$datasetName/training")) {
                            Dataset.Train
                        } else if (chunks.startsWith("$datasetName/test")) {
                            Dataset.Test
                        } else {
                            // assert(chunks.startsWith("$datasetName/validation"))
                            Dataset.Val
                        }
                        println(">>> dataset type: $dataset")
                        // File(outputPath).appendText("${vFile.canonicalPath}\n")


                        //parse file
                        if (!vFile.isDirectory) {
                            JavaParser().parse(vFile.inputStream)?.let { fileNode ->
                                //extract method nodes
                                val methods = JavaMethodSplitter().splitIntoMethods(fileNode)

                                methods.forEach { methodInfo ->
                                    val methodNameNode = methodInfo.method.nameNode ?: return@forEach
                                    val methodRoot = methodInfo.method.root
                                    val label = splitToSubtokens(methodNameNode.getToken()).joinToString("|")
                                    methodRoot.preOrder().forEach { it.setNormalizedToken() }
                                    methodNameNode.setNormalizedToken("METHOD_NAME")

                                    // Retrieve paths from every node individually
                                    val paths = minerGold.retrievePaths(methodRoot)
                                    storageGold.store(XLabeledPathContexts(
                                        label = label,
                                        pathContexts = paths.map {
                                            toXPathContext(
                                                path = it,
                                                getToken = { node -> node.getNormalizedToken() },
                                                getTokenType = { node -> "unknown" }
                                            )
                                        }
                                    ), dataset)
                                }
                            }
                        }

                        /**
                         * Processing START
                         */
                        psi?.let { it ->
                            val rootNode = convertPSITree(it)
                            val methods = JavaMethodSplitterFromPsi().splitIntoMethods(rootNode)

                            methods.forEach { methodInfo ->
                                val methodNameNode = methodInfo.method.nameNode ?: return@forEach
                                val methodRoot = methodInfo.method.root
                                val label = splitToSubtokens(methodNameNode.getToken()).joinToString("|")
                                methodRoot.preOrder().forEach { it.setNormalizedToken() }
                                methodNameNode.setNormalizedToken("METHOD_NAME")

                                // Retrieve paths from every node individually
                                val paths = miner.retrievePaths(methodRoot)
                                storage.store(XLabeledPathContexts(
                                    label = label,
                                    pathContexts = paths.map {
                                        toXPathContext(
                                            path = it,
                                            getToken = { node -> node.getNormalizedToken() },
                                            getTokenType = {node2type.getOrDefault(it, "unknown")}
                                        )
                                    }
                                ), dataset)
                            }
                        }

                        /**
                         * Processing END
                         */
                        node2type.clear()

                        true
                    }
                }
            } / 1000

            println("saving astminer-based a.k.a. gold...")
            storageGold.save()
            println("saving psi-based...")
            storage.save()
            println("\nCOMPUTED IN $executionTime SECONDS\n")
            println("Processing files...DONE! [$executionTime sec]")
            exitProcess(0)
        }
    }
}


// conversion

fun convertPSITree(root: PsiElement): SimpleNode {
    val tree = convertPsiElement(root, null)
    return compressTree(tree)
}

fun convertPsiElement(node: PsiElement, parent: SimpleNode?): SimpleNode {
    val currentNode = SimpleNode(node.elementType.toString(), parent, null)
    val children: MutableList<Node> = ArrayList()

    node.children
        .filter {
            !(it is PsiWhiteSpace || it is PsiDocComment || it is PsiImportStatement || it is PsiPackageStatement)
                    && !(it is PsiMethod && it.isConstructor)
        }
        .forEach {
        when (it) {
            is PsiJavaToken -> {
                val n = SimpleNode(it.tokenType.toString(), currentNode, it.text)
                children.add(n)
            }
            is PsiThisExpression -> {
                val token = "this"
                val tokenType = it.type?.presentableText ?: "null"
                val n = SimpleNode(it.elementType.toString(), currentNode, token)
                node2type[n] = tokenType
                children.add(n)
            }
            is PsiReferenceExpression -> {
                val token = it.element.text
                val tokenType = it.type?.presentableText ?: "null"
                val n = SimpleNode(it.elementType.toString(), currentNode, token)
                node2type[n] = tokenType
                children.add(n)
            }
            is PsiVariable -> {
                val token = it.name
                val tokenType = it.type.presentableText ?: "null"
                val n = SimpleNode(it.elementType.toString(), currentNode, token)
                node2type[n] = tokenType
                children.add(n)
                it.children.forEach { kid -> convertPsiElement(kid, n) }
            }
            /* TODO: consider creating ParameterNode at this point
            is PsiParameterList -> {
                it.parameters.forEach {
                    val returnTypeNodePsi = SimpleNode(it.type.canonicalText, null, it.elementType.toString())
                    val nameNodePsi = it.nameIdentifier

//                    val returnTypeNode = convertPsiElement(returnTypeNodePsi as PsiElement, null)
                    val nameNode = convertPsiElement(nameNodePsi as PsiElement, null)

                    children.add(
                        ParameterNode(it.asSimpleNode(), returnTypeNodePsi, nameNode) as Node
                    )
                }
            }
            */
//            is PsiMethod -> {
//                if (!it.isConstructor) {
//                    children.add(convertPsiElement(it, currentNode))
//                }
//            }
            else -> {
                children.add(convertPsiElement(it, currentNode))
            }
        }
    }
    currentNode.setChildren(children)

    return currentNode
}


/**
 * Method Splitter
 */

fun Node.getChildWithPrefix(prefix: String): Node? {
    return getChildren().filter { it.getTypeLabel().startsWith(prefix) }.firstOrNull()
}

class JavaMethodSplitterFromPsi : TreeMethodSplitter<SimpleNode> {
    companion object {
        private const val METHOD_NODE = "METHOD"
        private const val METHOD_RETURN_TYPE_NODE = "TYPE" // "typeTypeOrVoid"
        private const val METHOD_NAME_NODE = "IDENTIFIER"

        private const val CLASS_DECLARATION_NODE = "CLASS" // "classDeclaration"
        private const val CLASS_NAME_NODE = "IDENTIFIER"

        private const val METHOD_PARAMETER_NODE = "PARAMETER_LIST" // "formalParameters"
        private const val METHOD_PARAMETER_INNER_NODE = "PARAMETER"
        private val METHOD_SINGLE_PARAMETER_NODE = listOf("formalParameter", "lastFormalParameter")
        private const val PARAMETER_RETURN_TYPE_NODE = "TYPE" // "typeType"
        private const val PARAMETER_NAME_NODE = "IDENTIFIER" // "variableDeclaratorId"
    }

    override fun splitIntoMethods(root: SimpleNode): Collection<MethodInfo<SimpleNode>> {
        val methodRoots = root.preOrder().filter {
            decompressTypeLabel(it.getTypeLabel()).last() == METHOD_NODE
        }
        return methodRoots.map { collectMethodInfo(it as SimpleNode) }
    }

    private fun collectMethodInfo(methodNode: SimpleNode): MethodInfo<SimpleNode> {
        // 1. Extract Method Info
        val methodName = methodNode.getChildOfType(METHOD_NAME_NODE) as? SimpleNode
        val methodReturnTypeNode = methodNode.getChildOfType("TYPE")?.let {
            it.getChildren().firstOrNull() ?: it
        } as? SimpleNode
        // TODO: do we really need it? examples?
        methodReturnTypeNode?.setToken(collectParameterToken(methodReturnTypeNode))

        // 2. Extract Class Info
        val classRoot = getEnclosingClass(methodNode)
        val className = classRoot?.getChildOfType(CLASS_NAME_NODE) as? SimpleNode

        val parametersRoot = methodNode.getChildOfType(METHOD_PARAMETER_NODE) as? SimpleNode
        val innerParametersRoot = parametersRoot?.getChildOfType(METHOD_PARAMETER_INNER_NODE) as? SimpleNode

        /* TODO: verify
        val parametersList = when {
            innerParametersRoot != null -> getListOfParameters(innerParametersRoot)
            parametersRoot != null -> getListOfParameters(parametersRoot)
            else -> emptyList()
        }
         */
        val parametersList = emptyList<ParameterNode<SimpleNode>>()

        return MethodInfo(
            MethodNode(methodNode, methodReturnTypeNode, methodName),
            ElementNode(classRoot, className),
            parametersList
        )
    }

    private fun getEnclosingClass(node: SimpleNode): SimpleNode? {
        if (decompressTypeLabel(node.getTypeLabel()).last() == CLASS_DECLARATION_NODE) {
            return node
        }
        val parentNode = node.getParent() as? SimpleNode
        if (parentNode != null) {
            return getEnclosingClass(parentNode)
        }
        return null
    }

    private fun getListOfParameters(parametersRoot: SimpleNode): List<ParameterNode<SimpleNode>> {
        if (METHOD_SINGLE_PARAMETER_NODE.contains(decompressTypeLabel(parametersRoot.getTypeLabel()).last())) {
            return listOf(getParameterInfoFromNode(parametersRoot))
        }
        return parametersRoot.getChildren().filter {
            val firstType = decompressTypeLabel(it.getTypeLabel()).first()
            METHOD_SINGLE_PARAMETER_NODE.contains(firstType)
        }.map {
            getParameterInfoFromNode(it as SimpleNode)
        }
    }

    private fun getParameterInfoFromNode(parameterRoot: SimpleNode): ParameterNode<SimpleNode> {
        val returnTypeNode = parameterRoot.getChildOfType(PARAMETER_RETURN_TYPE_NODE) as? SimpleNode
        returnTypeNode?.setToken(collectParameterToken(returnTypeNode))
        return ParameterNode(
            parameterRoot,
            returnTypeNode,
            parameterRoot.getChildOfType(PARAMETER_NAME_NODE) as? SimpleNode
        )
    }

    private fun collectParameterToken(parameterRoot: SimpleNode): String {
        if (parameterRoot.isLeaf()) {
            return parameterRoot.getToken()
        }
        return parameterRoot.getChildren().joinToString(separator = "") { child ->
            collectParameterToken(child as SimpleNode)
        }
    }
}