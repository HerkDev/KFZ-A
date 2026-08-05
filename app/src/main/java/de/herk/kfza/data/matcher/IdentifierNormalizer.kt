package de.herk.kfza.data.matcher

object IdentifierNormalizer {
    private val whitespace = Regex("\\s+")

    fun normalize(input: String): String =
        input.trim().uppercase().replace(whitespace, "")
}