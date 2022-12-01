package storage.graphs

import JavaPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import labelextractor.LabeledTree
import labelextractor.StringLabel
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import psi.graphs.graphMiners.JavaGraphMiner
import java.io.File

internal class JsonGraphStorageTest : JavaPsiRequiredTest("JavaMethods") {

    @ParameterizedTest
    @ValueSource(
        strings = ["abstractMethod", "emptyMethod", "smallMethod", "largeMethod", "recursiveMethod"]
    )
    fun `test ignoring in Kotlin methods`(methodName: String) = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        val graphMiner = JavaGraphMiner()
        val jsonGraphStorage = JsonGraphStorage(File("."), graphMiner)
        println(jsonGraphStorage.convert(LabeledTree(psiRoot, StringLabel("myLabel")), null))
    }
}
