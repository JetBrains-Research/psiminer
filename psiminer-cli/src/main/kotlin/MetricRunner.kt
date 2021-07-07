import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ApplicationStarter
import java.io.File
import kotlin.system.exitProcess


class MetricRunner : ApplicationStarter {
    override fun getCommandName(): String = "metric"




    private val metrics = listOf(ResolvedImportsMetric, ResolvedReferencesMetric, ResolvedDependenciesMetric)

    val projectFile = File("/Users/Egor.Porsev/Documents/psiminer/psiminer-core/src/test/resources/mock_data")

    override fun main(args: Array<out String>) = ApplicationManager.getApplication().runReadAction {
        val project = openProject(projectFile) ?: error("Project didnt open")
        val outputFile = File("/Users/Egor.Porsev/Documents/psiminer/output/results.csv").apply { createNewFile() }

        println("Project opened")

        ResultsTable(metrics, outputFile).use { resultsTable ->
            resultsTable.addProject(project)
        }
        exitProcess(0)
    }
}



