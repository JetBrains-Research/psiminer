import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.psi.*

class DumpPSIAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        println("Dumping! :)")

        /*
        e.psiFile()?.let { psiFile ->
            println(psiFile)
            println(DebugUtil.psiToString(psiFile, false, true))
            println("*****")
            psiFile.viewProvider.allFiles.forEach {
                it?.let {file ->
                    println(file)
                    println(DebugUtil.psiToString(file, false, true))
                    println("=====")
                }
            }
        }
        */

        e.psiFile()?.code2VecInfoWithAstMiner("")

        /*
        println(DebugUtil.psiToString(e.psiFile()!!, true))

        PsiTreeUtil.collectElementsOfType(e.psiFile(), LeafPsiElement::class.java).forEach {
            println(">>> LeafPsiElement: ${it.javaClass.name} – ${it.elementType}")
            if (it.elementType.toString() == "IDENTIFIER") {
                
                println(">>>>>> ${SymbolService.resolveResult(it).target}")
                println(">>>>>> ${SymbolService.resolveResult(it).target.javaClass}")
            }
        }
        */

        /*
        e.psiFile()?.let {
            it.accept(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
//                    element?.let {
//                        println(it.javaClass)
//                        println(DebugUtil.psiToString(element, true))
//                    }
                    when (element) {
                        is PsiMethod -> {
                            println("[PSI_METHOD]: '${element.name}'")

                            element.accept(object : PsiRecursiveElementWalkingVisitor() {
                                override fun visitElement(element: PsiElement) {
                                    when (element) {
                                        is PsiVariable -> {
                                            println("name '${element.name}' | type '${element.type}'")
                                        }
                                    }
                                    super.visitElement(element)
                                }
                            })
//                            element.body?.children?.filterIsInstance<PsiLocalVariable>()?.forEach {
//                                println("${it} ${it.elementType}")
//                            }
//                            element.body?.statements?.filter { it is PsiDeclarationStatement }?.forEach {
//                                println("${it} ${it.elementType}")
//                            }
                        }
//                        is PsiLocalVariable -> {
//                            println("[PSI_LOCAL_VARIABLE]: ${element.javaClass.name} – ${element.elementType}")
//                            element.parents
//                        }
//                        is com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl -> {
//                            println("> LocalVariable: ${element.javaClass.name} – ${element.elementType}")
//                        }
//                        is LeafPsiElement -> {
////                            (element as LeafPsiElement).
//                            println(">>> LeafPsiElement: ${element.javaClass.name} – ${element.elementType}")
//                        }
//                        is  -> {
//                            println("[PSI_METHOD]: ${element.javaClass.name} – ${element.elementType}")
//                        }
////                        is KtNamedFunctio
                    }

                    super.visitElement(element)
                }
            })
        }
         */

        println("Dumping DONE.")
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.psiFile() != null
    }
}

//fun AnActionEvent.psiFile() = LangDataKeys.PSI_FILE.getData(this.dataContext)
fun AnActionEvent.psiFile() = this.getData(LangDataKeys.PSI_FILE)
