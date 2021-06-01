import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.serviceContainer.AlreadyDisposedException
import java.io.File

/***
 * Function to open the project by given file.
 * TODO: check that project is opened and all SDK is loaded
 * @param projectFile: file with project
 * @return: project if opening is successful or null in other case
 */
fun openProject(projectFile: File): Project? =
    ProjectUtil.openOrImport(projectFile.path, null, true)

/***
 * Function to close project. The close should be forced to avoid physical changes to data.
 * TODO: Avoid using extended API (check if available in free version)
 */
fun closeProject(project: Project) =
    try {
        ProjectManagerEx.getInstanceEx().forceCloseProject(project)
    } catch (e: AlreadyDisposedException) {
        // TODO: figure out why this happened
        println(e.message)
    }

/***
 * Collect all files from project that correspond to given language
 * Search is based on checking extension of each file
 * @param project: project where run search
 * @param language: specified language
 * @return: list of all files in project that correspond to required language
 */
fun getAllFilesByLanguage(project: Project, language: Language) =
    ProjectRootManager
        .getInstance(project)
        .contentRoots
        .flatMap { root ->
            VfsUtil.collectChildrenRecursively(root).filter {
                it.extension in language.extensions && it.canonicalPath != null
            }
        }
        .filterNotNull()
