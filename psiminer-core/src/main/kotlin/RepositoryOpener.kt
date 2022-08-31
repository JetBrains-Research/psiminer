import com.intellij.ide.impl.OpenProjectTask
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import org.jetbrains.research.pluginUtilities.openRepository.getKotlinJavaRepositoryOpener
import org.jetbrains.research.pluginUtilities.preprocessing.PreprocessorManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

open class RepositoryOpener {
    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    open fun openRepository(repositoryRoot: File, onOpen: (Project) -> Unit) {
        ProjectUtil.getOpenProjects().forEach { ProjectManagerEx.getInstance().closeAndDispose(it) }
        val project = ProjectUtil.openOrImport(repositoryRoot.toPath(), OpenProjectTask.build().asNewProject())
        if (project == null) {
            logger.warn("Unable to open project in ${repositoryRoot.path}")
            return
        }
        project.apply(onOpen)
        ProjectManagerEx.getInstance().closeAndDispose(project)
    }
}

class JVMRepositoryOpener(private val preprocessorManager: PreprocessorManager?) : RepositoryOpener() {

    private val repositoryOpener = getKotlinJavaRepositoryOpener()

    override fun openRepository(repositoryRoot: File, onOpen: (Project) -> Unit) {
        preprocessorManager?.preprocessRepositoryInplace(repositoryRoot)
        val allProjectsOpenedSuccessfully = repositoryOpener.openRepository(repositoryRoot, onOpen)
        if (!allProjectsOpenedSuccessfully) {
            logger.warn("Some projects in repository ${repositoryRoot.name} were opened with exceptions")
        }
    }
}
