import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.*
import java.io.File
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

class Runner : ApplicationStarter {

    override fun getCommandName(): String = "code2vec"

    override fun main(args: Array<out String>) {
        println("Hello Plugin!")

        if (args.size != 3) {
            println("incorrect number of arguments!")
            exitProcess(0)
        }

        val projectPath = args[1] // "/Users/petukhov/Downloads/c2v_data/small/test/hadoop"
        val outputPath = args[2] // "/Users/petukhov/Downloads/c2v_data/small/test/hadoop.c2v.txt"

        val project = ProjectUtil.openOrImport(projectPath, null, true)
        if (project == null) {
            println("Could not load project from $projectPath")
        } else {
            println("Opened project")
            // val sdkName = ProjectRootManager.getInstance(project).projectSdkName
            // println(sdkName)
            println("Processing files...")
            val executionTime = measureTimeMillis {
                ProjectRootManager.getInstance(project).contentRoots.forEach { root ->
                    VfsUtilCore.iterateChildrenRecursively(root, null) { vFile ->
                        val psi = PsiManager.getInstance(project).findFile(vFile)
                        println("${vFile.canonicalPath}")
                        File(outputPath).appendText("${vFile.canonicalPath}\n")
                        File(outputPath).appendText(
                            (psi?.code2VecInfo()?.joinToString("\n") ?: "Nothing") + "\n\n"
                        )
                        true
                    }
                }
            } / 1000
            File(outputPath).appendText("\nCOMPUTED IN $executionTime SECONDS\n")
            println("Processing files...DONE! [$executionTime sec]")
            exitProcess(0)
//            PsiManager.getInstance(project).findFile()
//            PsiDocumentManager.getInstance(project).
        }
    }
}

fun PsiFile.code2VecInfo(): List<String> {
    val result = mutableListOf<String>()
    this.accept(object : PsiRecursiveElementWalkingVisitor() {
        override fun visitElement(element: PsiElement) {
            when (element) {
                is PsiMethod -> {
                    result.add("[PSI_METHOD]: '${element.name}'")

                    element.accept(object : PsiRecursiveElementWalkingVisitor() {
                        override fun visitElement(element: PsiElement) {
                            when (element) {
                                is PsiVariable -> {
                                    result.add("variable '${element.name}' | type '${element.type.presentableText}'")
                                }
                            }
                            super.visitElement(element)
                        }
                    })
                }
            }
            super.visitElement(element)
        }
    })
    return result
}
