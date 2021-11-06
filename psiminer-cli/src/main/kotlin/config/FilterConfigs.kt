package config

import filter.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
abstract class FilterConfig {
    abstract fun createFilter(): Filter
}

// ========== Code based filter configs ==========

@Serializable
@SerialName("code lines")
class CodeLinesFilterConfig(
    private val minCodeLines: Int = 0, // Set the minimum number of lines in corresponded code snippet
    private val maxCodeLines: Int? = null // Set the maximum number of lines in corresponded code snippet
) : FilterConfig() {
    override fun createFilter(): Filter = CodeLinesFilter(minCodeLines, maxCodeLines)
}

// ========== Method filter configs ==========

@Serializable
@SerialName("constructor")
class ConstructorsFilterConfig : FilterConfig() {
    override fun createFilter(): Filter = ConstructorFilter()
}

@Serializable
@SerialName("by modifiers")
class ModifiersFilterConfig(private val excludeModifiers: List<String>) : FilterConfig() {
    override fun createFilter(): Filter = ModifierFilter(excludeModifiers)
}

@Serializable
@SerialName("by annotations")
class AnnotationsFilterConfig(private val excludeAnnotations: List<String>) : FilterConfig() {
    override fun createFilter(): Filter = AnnotationFilter(excludeAnnotations)
}

@Serializable
@SerialName("empty method")
class EmptyMethodFilterConfig : FilterConfig() {
    override fun createFilter(): Filter = EmptyMethodFilter()
}

// ========== Tree based configs ==========

@Serializable
@SerialName("tree size")
class TreeSizeFilterConfig(
    private val minSize: Int = 0, // Set the minimum number of nodes in target trees
    private val maxSize: Int? = null // Set the maximum number of nodes in target trees
) : FilterConfig() {
    override fun createFilter(): Filter = TreeSizeFilter(minSize, maxSize)
}
