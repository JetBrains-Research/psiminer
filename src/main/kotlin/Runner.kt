import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationStarter
import storage.XCode2SeqPathStorage
import kotlin.system.exitProcess

class Runner : ApplicationStarter {

    override fun getCommandName(): String = "psiminer"

    override fun main(args: Array<out String>) {
        println("Start mining PSI paths from input data")

        if (args.size != 3) {
            println("You should specify path to the folder with input data and to the output folder")
            exitProcess(0)
        }

        val projectPath = args[1]
        val outputPath = args[2]

        println("Opening data as a project...")
        val project = ProjectUtil.openOrImport(projectPath, null, true)
        if (project == null) {
            println("Could not load project from $projectPath")
            exitProcess(0)
        }

        val storage = XCode2SeqPathStorage<String>(outputPath)
        val datasetStatistic = extractPsiFromProject(project, storage)
        println("Extracted data statistic:\n$datasetStatistic")
        exitProcess(0)
    }
}
