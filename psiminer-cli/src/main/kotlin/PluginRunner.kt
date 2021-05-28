import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import com.intellij.openapi.application.ApplicationStarter
import config.*
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
    polymorphic(ProblemConfig::class) {
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
}

class PsiExtractor : CliktCommand() {

    private val dataset by argument(help = "Path to dataset").file(mustExist = true, canBeFile = false)
    private val output by argument(help = "Output directory").file(canBeFile = false)
    private val jsonConfig by argument(help = "JSON config").file(mustExist = true, canBeDir = false)

    override fun run() {
        val config = jsonFormat.decodeFromString<Config>(jsonConfig.readText())

        val filters = config.filters.map { it.createFilter() }
        val problem = config.problem.createProblem()
        val storage = config.storage.createStorage(output)
        val pipeline = Pipeline(filters, problem, storage)

        try {
            pipeline.extract(
                dataset,
                config.languages,
                config.ignoreRules.map { it.createIgnoreRule() },
                config.treeProcessors.map { it.createTreeProcessor() },
                config.printTrees
            )
        } catch (e: Exception) {
            println(e.message)
        } finally {
            storage.printStatistic()
            storage.close()
            exitProcess(0)
        }
    }
}
