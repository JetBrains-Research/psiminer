import astminer.paths.PathMiner
import astminer.paths.PathRetrievalSettings
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.intellij.openapi.application.ApplicationStarter
import psi.extractPsiFromDataset
import storage.XCode2SeqPathStorage
import storage.XCode2VecPathStorage
import storage.XPathContextsStorage
import kotlin.system.exitProcess

class PluginRunner : ApplicationStarter {

    override fun getCommandName(): String = "psiminer"

    override fun main(args: Array<out String>) {
        println(args.joinToString(" "))
        PsiExtractor().main(args.slice(1 until args.size))
    }
}

class PsiExtractor : CliktCommand() {

    private val dataset: String by option("--dataset", help = "Path to dataset").required()
    private val output: String by option("--output", help = "Output directory").required()
    private val noTypes: Boolean by option("--no-types", help = "Extract paths without types")
            .flag(default = false)

    private fun buildStorage(): XPathContextsStorage<String> {
        return when (Config.storage) {
            "code2seq" -> XCode2SeqPathStorage(output, noTypes)
            "code2vec" -> XCode2VecPathStorage(output, noTypes)
            else -> throw UnsupportedOperationException("Unknown storage ${Config.storage}")
        }
    }

    override fun run() {
        val storage = buildStorage()
        val miner = PathMiner(PathRetrievalSettings(
            Config.maxPathHeight,
            Config.maxPathWidth
        ))

        println("Start extracting PSI from $dataset dataset...")
        val datasetStatistic = extractPsiFromDataset(dataset, storage, miner)
        println("Extracted data statistic:\n$datasetStatistic")

        storage.close()
        exitProcess(0)
    }
}
