import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys

class DumpPSIAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        println("Dumping! :)")
        e.psiFile()?.runPlugin()
        println("Dumping DONE.")
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.psiFile() != null
    }
}

//fun AnActionEvent.psiFile() = LangDataKeys.PSI_FILE.getData(this.dataContext)
fun AnActionEvent.psiFile() = this.getData(LangDataKeys.PSI_FILE)
