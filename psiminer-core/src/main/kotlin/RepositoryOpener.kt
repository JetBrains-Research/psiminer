import com.intellij.ide.impl.OpenProjectTask
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.ex.ActionManagerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import org.jetbrains.research.pluginUtilities.openRepository.getKotlinJavaRepositoryOpener
import org.jetbrains.research.pluginUtilities.preprocessing.PreprocessorManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

open class RepositoryOpener {
    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    open fun openRepository(repositoryRoot: File, onOpen: (Project) -> Boolean) {
        println("Opening project at ${repositoryRoot.absolutePath}")
        ProjectUtil.getOpenProjects().forEach { ProjectManagerEx.getInstance().closeAndDispose(it) }
        ActionManagerEx.getInstanceEx() // workaround for https://youtrack.jetbrains.com/issue/IDEA-294726
        val task = OpenProjectTask.build()
        val project = ProjectUtil.openOrImport(repositoryRoot.toPath(), task)
        if (project == null) {
            logger.warn("Unable to open project in ${repositoryRoot.path}")
            return
        }
        project.let(onOpen)
        ProjectManagerEx.getInstance().closeAndDispose(project)
    }
}

class JVMRepositoryOpener(private val preprocessorManager: PreprocessorManager?) : RepositoryOpener() {

    private val repositoryOpener = getKotlinJavaRepositoryOpener()

    override fun openRepository(repositoryRoot: File, onOpen: (Project) -> Boolean) {
        preprocessorManager?.preprocessRepositoryInplace(repositoryRoot)
        val allProjectsOpenedSuccessfully = repositoryOpener.openRepository(repositoryRoot, onOpen)
        if (!allProjectsOpenedSuccessfully) {
            logger.warn("Some projects in repository ${repositoryRoot.name} were opened with exceptions")
        }
    }
}
