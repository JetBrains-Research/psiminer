import astminer.common.getNormalizedToken
import astminer.common.model.Node
import astminer.common.preOrder
import astminer.common.setNormalizedToken
import astminer.common.splitToSubtokens
import astminer.paths.PathMiner
import astminer.paths.PathRetrievalSettings
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import psi.PsiMethodSplitter
import psi.convertPSITree
import java.io.File
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis


fun PsiElement.runPlugin() {
    // debug it here in IntelliJ
}

val node2type: MutableMap<Node, String> = hashMapOf()

//val mapper = ObjectMapper().registerModule(KotlinModule())

class Runner : ApplicationStarter {

    override fun getCommandName(): String = "psiminer"

    override fun main(args: Array<out String>) {
        println("Start mining PSI paths from input data")

        // System.setProperty("org.litote.mongo.test.mapping.service", "org.litote.kmongo.jackson.JacksonClassMappingTypeService")

        if (args.size != 3) {
            println("You should specify path to the folder with input data and to the output folder")
            exitProcess(0)
        }

        val projectPath = args[1]
        val outputPath = args[2]
        val absoluteProjectPath = File(System.getProperty("user.dir")).resolve(projectPath)

        println("Opening data as a project...")
        val project = ProjectUtil.openOrImport(projectPath, null, true)
        if (project == null) {
            println("Could not load project from $projectPath")
            exitProcess(0)
        }

        println("Processing data...")
        val miner = PathMiner(PathRetrievalSettings(5, 5))
        val storage = XCode2VecPathStorage("astminer_withTypes", "$outputPath/withTypes")

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
                    vFile.canonicalPath ?: return@iterateChildrenRecursively true

                    // verify path
//                    val chunks = vFile.canonicalPath?.split("/")
//                            ?.drop(4)
//                            ?.joinToString("/", prefix = "/")
//                            ?: ""
//                    val datasetName = "/java-med"
//                    if (!chunks.startsWith(datasetName)) {
//                        println("skipping! chunks: $chunks")
//                        return@iterateChildrenRecursively true
//                    } else {
//                        println("👍🏻 chunks: $chunks")
//                    }
                    val filename = File(vFile.canonicalPath ?: "").relativeTo(absoluteProjectPath)
                    println("processing $filename $index / $total")
                    if (filename.extension != "java") {
                        println("skip $filename")
                        return@iterateChildrenRecursively true
                    }

                    // determine if it's train / val / test
                    // val dataset = Dataset.valueOf()
                    val dataset = when {
                        filename.startsWith("training") -> Dataset.Train
                        filename.startsWith("test") -> Dataset.Test
                        filename.startsWith("validation") -> Dataset.Val
                        else -> {
                            println("skip $filename")
                            return@iterateChildrenRecursively true
                        }
                    }
                    println(">>> dataset type: $dataset")
                    // File(outputPath).appendText("${vFile.canonicalPath}\n")

                    /**
                     * Processing START
                     */
                    psi?.let { it ->
                        val rootNode = convertPSITree(it)
                        val methods = PsiMethodSplitter().splitIntoMethods(rootNode)

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
                                    pathContexts = paths.map { it ->
                                        toXPathContext(
                                                path = it,
                                                getToken = { node -> node.getNormalizedToken() },
                                                getTokenType = { node2type.getOrDefault(it, "unknown") }
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

        println("saving psi-based...")
        storage.save()
        println("\nCOMPUTED IN $executionTime SECONDS\n")
        println("Processing files...DONE! [$executionTime sec]")
        exitProcess(0)
    }
}
