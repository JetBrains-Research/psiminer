import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import com.intellij.openapi.application.ApplicationStarter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.system.exitProcess

class PluginRunner : ApplicationStarter {

    override fun getCommandName(): String = "psiminer"

    override fun main(args: Array<out String>) {
        PsiExtractor().main(args.slice(1 until args.size))
    }
}

class PsiExtractor : CliktCommand() {

    private val dataset by argument(help = "Path to dataset").file(mustExist = true, canBeFile = false)
    private val output by argument(help = "Output directory").file(canBeFile = false)
    private val jsonConfig by argument(help = "JSON config").file(mustExist = true, canBeDir = true)

    override fun run() {
        try {
            val config = Json.decodeFromString<Config>(jsonConfig.readText())
            val pipeliner = Pipeline(output, config)
            pipeliner.extractDataFromDataset(dataset)
        } catch (e: IllegalArgumentException) {
            println(e.message)
        } finally {
            exitProcess(0)
        }
    }
}
