package labelextractor

interface Label {
  fun getStringRepresentation(): String
}

class StringLabel(private val value: String) : Label {
    override fun getStringRepresentation(): String {
        return value
    }
}

class StringListLabel(private val value: List<String>) : Label {
    override fun getStringRepresentation(): String {
        return value.joinToString(",")
    }
}
