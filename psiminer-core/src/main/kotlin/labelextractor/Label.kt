package labelextractor

interface Label{
  fun getString(): String
}

class StringLabel(private val value: String): Label{
    override fun getString(): String {
        return value
    }
}

class StringListLabel(private val value: List<String>): Label{
    override fun getString(): String {
        return value.joinToString(",")
    }
}
