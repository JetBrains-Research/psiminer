import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import com.intellij.openapi.application.ApplicationStarter
import config.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.system.exitProcess

class PluginRunner : ApplicationStarter {

    override fun getCommandName(): String = "psiminer"

    override fun main(args: Array<out String>) {
        PsiExtractor().main(args.slice(1 until args.size))
    }
}

val module = SerializersModule {
    polymorphic(StorageConfig::class) {
        subclass(JsonTreeStorageConfig::class)
        subclass(JsonTypedTreeStorageConfig::class)
        subclass(Code2SeqStorageConfig::class)
        subclass(TypedCode2SeqStorageConfig::class)
    }
    polymorphic(FilterConfig::class) {
        subclass(CodeLinesFilterConfig::class)
        subclass(TreeSizeFilterConfig::class)
        subclass(ConstructorsFilterConfig::class)
        subclass(ModifiersFilterConfig::class)
        subclass(AnnotationsFilterConfig::class)
        subclass(EmptyMethodFilterConfig::class)
    }
    polymorphic(LabelExtractorConfig::class) {
        subclass(MethodNamePredictionConfig::class)
    }
    polymorphic(PsiNodeIgnoreRuleConfig::class) {
        subclass(WhiteSpaceIgnoreRuleConfig::class)
        subclass(KeywordsIgnoreRuleConfig::class)
        subclass(EmptyListsIgnoreRuleConfig::class)
        subclass(PackageStatementIgnoreRuleConfig::class)
        subclass(ImportStatementIgnoreRuleConfig::class)
        subclass(JavaSymbolsIgnoreRuleConfig::class)
    }
    polymorphic(PsiTreeProcessorConfig::class) {
        subclass(HideLiteralsConfig::class)
        subclass(CompressOperatorsConfig::class)
        subclass(RemoveCommentsConfig::class)
    }
}

val jsonFormat = Json {
    serializersModule = module
    classDiscriminator = "name"
    ignoreUnknownKeys = false
}

class PsiExtractor : CliktCommand() {

    private val dataset by argument(help = "Path to dataset").file(mustExist = true, canBeFile = false)
    private val output by argument(help = "Output directory").file(canBeFile = false)
    private val jsonConfig by argument(help = "JSON config").file(mustExist = true, canBeDir = false)

    override fun run() {
        try {
            val config = jsonFormat.decodeFromString<Config>(jsonConfig.readText())

            val storage = config.storage.createStorage(output)
            val pipelineConfig = PipelineConfig(
                parameters = Parameters(config.batchSize, config.printTrees),
                languages = config.languages,
                nodeIgnoreRules = config.nodeIgnoreRules.map { it.createIgnoreRule() },
                treeTransformations = config.treeTransformers.map { it.createTreeProcessor() },
                filters = config.filters.map { it.createFilter() },
                labelExtractor = config.labelExtractor.createProblem(),
                storage = storage
            )
            val pipeline = Pipeline(pipelineConfig)
            pipeline.extract(dataset)
            storage.printStatistic()
            storage.close()
        } catch (e: SerializationException) {
            println("Error during parsing the config:\n${e.message}")
        } catch (e: Exception) {
            println(e.message)
        } finally {
            exitProcess(0)
        }
    }
}
