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
import org.jetbrains.research.pluginUtilities.openRepository.getKotlinJavaRepositoryOpener
import org.jetbrains.research.pluginUtilities.preprocessing.getKotlinJavaPreprocessorManager
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
        subclass(Code2SeqStorageConfig::class)
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
    ignoreUnknownKeys = false
}

class PsiExtractor : CliktCommand() {

    private val dataset by argument(help = "Path to dataset").file(mustExist = true, canBeFile = false)
    private val output by argument(help = "Output directory").file(canBeFile = false)
    private val jsonConfig by argument(help = "JSON config").file(mustExist = true, canBeDir = false)

    override fun run() {
        val config = try {
            jsonFormat.decodeFromString<Config>(jsonConfig.readText())
        } catch (e: SerializationException) {
            println("Error during parsing the config:\n${e.message}")
            exitProcess(1)
        }

        val storage = config.storage.createStorage(output)
        val pipeline = Pipeline(
            language = config.language,
            preprocessorManager = config.additionalPreprocessing?.let { getKotlinJavaPreprocessorManager(it.androidSdkPath) },
            repositoryOpener = getKotlinJavaRepositoryOpener(),
            psiTreeTransformations = config.treeTransformers.map { it.createTreeTransformation(config.language) },
            filters = config.filters.map { it.createFilter() },
            labelExtractor = config.labelExtractor.createProblem(),
            storage = storage
        )

        try {
            pipeline.extract(dataset, config.batchSize, config.printTrees)
            storage.printStatistic()
            storage.close()
            exitProcess(0)
        } catch (e: Exception) {
            println("Failed with ${e::class.simpleName}: ${e.message}")
            storage.close()
            exitProcess(1)
        }
    }
}
