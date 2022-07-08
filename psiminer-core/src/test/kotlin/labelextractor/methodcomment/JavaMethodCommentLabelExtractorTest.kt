package labelextractor.methodcomment

import JavaPsiRequiredTest
import com.intellij.openapi.application.ReadAction
import labelextractor.MethodCommentLabelExtractor
import org.junit.Assert
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class JavaMethodCommentLabelExtractorTest : JavaPsiRequiredTest("JavaMethods") {

    private val methodCommentLabelExtractor = MethodCommentLabelExtractor()

    @ParameterizedTest
    @MethodSource("provideParameters")
    fun `test method comment extraction`(methodName: String, commentLabel: String?) = ReadAction.run<Exception> {
        val psiRoot = getMethod(methodName)
        val methodCommentLabel = methodCommentLabelExtractor.handleTree(psiRoot, handler)
        if (methodCommentLabel == null) {
            Assert.assertNull(commentLabel)
        } else {
            Assert.assertEquals(commentLabel, methodCommentLabel.getStringRepresentation())
        }
    }

    private fun provideParameters(): Stream<Arguments> {
        return Stream.of(
            Arguments.of("sizeOf", "returns|the|size|of|this|big|array|in|bytes"),
            Arguments.of("smallMethod", null),
            Arguments.of("largeMethod", "this|is|java|doc")
        )
    }
}
