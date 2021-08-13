import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.project.Project
import java.io.File

/**
 * Function to open the project by given file.
 * TODO: check that project is opened and all SDK is loaded
 * @param projectFile: file with project
 * @return: project if opening is successful or null in other case
 */
fun openProject(projectFile: File): Project? =
    ProjectUtil.openOrImport(projectFile.path, null, true)
