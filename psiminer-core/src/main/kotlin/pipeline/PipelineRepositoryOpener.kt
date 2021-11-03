package pipeline

import com.intellij.openapi.project.Project
import org.jetbrains.research.pluginUtilities.openRepository.getKotlinJavaRepositoryOpener
import org.jetbrains.research.pluginUtilities.preprocessing.PreprocessorManager
import org.slf4j.LoggerFactory
import java.io.File

class PipelineRepositoryOpener(
    private val preprocessorManager: PreprocessorManager?,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val repositoryOpener = getKotlinJavaRepositoryOpener()

    fun openRepository(repositoryRoot: File, action: (Project) -> Unit) {
        preprocessorManager?.preprocessRepositoryInplace(repositoryRoot)
        val allProjectsOpenedSuccessfully = repositoryOpener.openRepository(repositoryRoot, action)
        if (!allProjectsOpenedSuccessfully) {
            logger.warn("Some projects in repository ${repositoryRoot.name} were opened with exceptions")
        }
    }
}
