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
@SerialName("CodeLines")
class CodeLinesFilterConfig(
    private val minCodeLines: Int = 0, // Set the minimum number of lines in corresponded code snippet
    private val maxCodeLines: Int? = null // Set the maximum number of lines in corresponded code snippet
) : FilterConfig() {
    override fun createFilter(): Filter = CodeLinesFilter(minCodeLines, maxCodeLines)
}

// ========== Method filter configs ==========

@Serializable
@SerialName("removeConstructor")
class ConstructorsFilterConfig : FilterConfig() {
    override fun createFilter(): Filter = ConstructorFilter()
}

@Serializable
@SerialName("removeAbstractMethod")
class AbstractMethodFilterConfig : FilterConfig() {
    override fun createFilter(): Filter = AbstractMethodFilter()
}

@Serializable
@SerialName("removeOverrideMethod")
class OverrideMethodFilterConfig : FilterConfig() {
    override fun createFilter(): Filter = OverrideMethodFilter()
}

@Serializable
@SerialName("removeEmptyMethod")
class EmptyMethodFilterConfig : FilterConfig() {
    override fun createFilter(): Filter = EmptyMethodFilter()
}

// ========== Tree based configs ==========

@Serializable
@SerialName("treeSize")
class TreeSizeFilterConfig(
    private val minSize: Int = 0, // Set the minimum number of nodes in target trees
    private val maxSize: Int? = null // Set the maximum number of nodes in target trees
) : FilterConfig() {
    override fun createFilter(): Filter = TreeBasedFilters(minSize, maxSize)
}
