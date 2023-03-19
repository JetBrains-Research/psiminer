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
import org.apache.log4j.PropertyConfigurator
import org.slf4j.LoggerFactory
import storage.StorageManager
import kotlin.system.exitProcess

class PluginRunner : ApplicationStarter {

    override fun getCommandName(): String = "psiminer"

    override fun main(args: Array<out String>) {
        val logPropertyFile = PluginRunner::class.java.getResource("log4j.properties")
        PropertyConfigurator.configure(logPropertyFile)

        PsiExtractor().main(args.slice(1 until args.size))
    }
}

val module = SerializersModule {
    polymorphic(PreprocessingConfig::class) {
        subclass(JVMPreprocessingConfig::class)
        default { DummyPreprocessingConfig.serializer() }
    }
    polymorphic(StorageConfig::class) {
        subclass(JsonTreeStorageConfig::class)
        subclass(Code2SeqStorageConfig::class)
        subclass(JsonGraphStorageConfig::class)
        subclass(PlainTextStorageConfig::class)
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
        subclass(MethodNameLabelExtractorConfig::class)
        subclass(MethodCommentLabelExtractorConfig::class)
    }
    polymorphic(PsiTreeTransformationConfig::class) {
        subclass(HideLiteralsTransformationConfig::class)
        subclass(CompressOperatorsTransformationConfig::class)
        subclass(RemoveCommentsTransformationConfig::class)
        subclass(ExcludeWhiteSpaceTransformationConfig::class)
        subclass(ExcludeKeywordTransformationConfig::class)
        subclass(ExcludeEmptyGrammarListTransformationConfig::class)
        subclass(ExcludePackageStatementTransformationConfig::class)
        subclass(ExcludeImportStatementsTransformationConfig::class)
        subclass(ExcludeLanguageSymbolsTransformationConfig::class)
        subclass(ResolveTypeTransformationConfig::class)
    }
}

val jsonFormat = Json {
    serializersModule = module
    classDiscriminator = "name"
    ignoreUnknownKeys = true
}

class PsiExtractor : CliktCommand() {

    private val dataset by argument(help = "Path to dataset").file(mustExist = true, canBeFile = false)
    private val output by argument(help = "Output directory").file(canBeFile = false)
    private val jsonConfig by argument(help = "JSON config").file(mustExist = true, canBeDir = false)

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run() {
        val config = try {
            jsonFormat.decodeFromString<Config>(jsonConfig.readText())
        } catch (e: SerializationException) {
            logger.error("Error during parsing the config:\n${e.message}")
            exitProcess(0)
        }

        val storageManager = StorageManager(
            createStorage = { config.storage.createStorage(it, config.language) },
            baseOutputDirectory = output
        )
        val pipeline = Pipeline(
            language = config.language,
            repositoryOpener = config.preprocessing.createPreprocessing(),
            psiTreeTransformations = config.treeTransformers.map { it.createTreeTransformation(config.language) },
            filters = config.filters.map { it.createFilter() },
            labelExtractor = config.labelExtractor.createProblem(),
            storageManager = storageManager,
            collectMetadata = config.collectMetadata
        )

        try {
            logger.warn("Start processing data.")
            pipeline.extract(dataset, config.numThreads, config.printTrees)
            storageManager.printStoragesStatistic()
            storageManager.closeStorages()
        } catch (e: Exception) {
            logger.error("Failed with ${e::class.simpleName}: ${e.message}")
            logger.error(e.stackTraceToString())
            storageManager.closeStorages()
        } finally {
            exitProcess(0)
        }
    }
}
