package fomatter

import BasePsiRequiredTest
import com.intellij.openapi.application.ReadAction
import formatter.tree.JsonTreeFormatter
import labelextractor.LabeledTree
import org.junit.Assert
import org.junit.jupiter.api.Test
import psi.transformations.typeresolve.resolvedTokenType

internal class JsonTreeFormatterTest : BasePsiRequiredTest() {

    @Test
    fun collectNodeRepresentationTest() = ReadAction.run<Exception> {
        val psiRoot = getJavaMethod(emptyMethod)
        psiRoot.resolvedTokenType = "type"
        val jsonTreeFormatter = JsonTreeFormatter(withPaths = false, withRanges = false)
        val actual = jsonTreeFormatter.collectNodeRepresentation(psiRoot)
        Assert.assertEquals(emptyMethodTree, actual)
    }

    @Test
    fun collectTreeRepresentationTest() = ReadAction.run<Exception> {
        val psiRoot = getJavaMethod(emptyMethod)
        psiRoot.resolvedTokenType = "type"
        val jsonTreeFormatter = JsonTreeFormatter(withPaths = false, withRanges = false)
        val actual = jsonTreeFormatter.collectTreeRepresentation(
            LabeledTree(
                psiRoot,
                "test label"
            )
        )
        val expected = JsonTreeFormatter.TreeRepresentation(
            "test label",
            null,
            emptyMethodTree
        )
        Assert.assertEquals(expected, actual)
    }

    companion object {
        const val emptyMethod = "emptyMethod"
        val emptyMethodTree = listOf(
            JsonTreeFormatter.NodeRepresentation(
                "EMPTY",
                "METHOD",
                "type",
                null,
                listOf(1, 3, 4, 5, 7, 8, 9, 12, 13, 14)
            ), JsonTreeFormatter.NodeRepresentation(
                "EMPTY",
                "MODIFIER_LIST",
                "<NT>",
                null,
                listOf(2)
            ), JsonTreeFormatter.NodeRepresentation(
                "public",
                "PUBLIC_KEYWORD",
                "<NT>",
                null,
                listOf()
            ), JsonTreeFormatter.NodeRepresentation(
                "EMPTY",
                "TYPE_PARAMETER_LIST",
                "<NT>",
                null,
                listOf()
            ), JsonTreeFormatter.NodeRepresentation(
                "EMPTY",
                "WHITE_SPACE",
                "<NT>",
                null,
                listOf()
            ), JsonTreeFormatter.NodeRepresentation(
                "EMPTY",
                "TYPE",
                "<NT>",
                null,
                listOf(6)
            ), JsonTreeFormatter.NodeRepresentation(
                "void",
                "VOID_KEYWORD",
                "<NT>",
                null,
                listOf()
            ), JsonTreeFormatter.NodeRepresentation(
                "EMPTY",
                "WHITE_SPACE",
                "<NT>",
                null,
                listOf()
            ), JsonTreeFormatter.NodeRepresentation(
                "empty|method",
                "IDENTIFIER",
                "<NT>",
                null,
                listOf()
            ), JsonTreeFormatter.NodeRepresentation(
                "EMPTY",
                "PARAMETER_LIST",
                "<NT>",
                null,
                listOf(10, 11)
            ), JsonTreeFormatter.NodeRepresentation(
                "(",
                "LPARENTH",
                "<NT>",
                null,
                listOf()
            ), JsonTreeFormatter.NodeRepresentation(
                ")",
                "RPARENTH",
                "<NT>",
                null,
                listOf()
            ), JsonTreeFormatter.NodeRepresentation(
                "EMPTY",
                "THROWS_LIST",
                "<NT>",
                null,
                listOf()
            ), JsonTreeFormatter.NodeRepresentation(
                "EMPTY",
                "WHITE_SPACE",
                "<NT>",
                null,
                listOf()
            ), JsonTreeFormatter.NodeRepresentation(
                "EMPTY",
                "CODE_BLOCK",
                "<NT>",
                null,
                listOf(15, 16, 17)
            ), JsonTreeFormatter.NodeRepresentation(
                "{",
                "LBRACE",
                "<NT>",
                null,
                listOf()
            ), JsonTreeFormatter.NodeRepresentation(
                "EMPTY",
                "WHITE_SPACE",
                "<NT>",
                null,
                listOf()
            ), JsonTreeFormatter.NodeRepresentation(
                "}",
                "RBRACE",
                "<NT>",
                null,
                listOf()
            )
        )
    }
}
