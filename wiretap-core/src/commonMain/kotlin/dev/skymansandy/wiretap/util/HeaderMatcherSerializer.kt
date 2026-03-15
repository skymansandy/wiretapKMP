package dev.skymansandy.wiretap.util

import dev.skymansandy.wiretap.domain.model.HeaderMatcher

/**
 * Serializes/deserializes a list of HeaderMatcher to/from a single string for DB storage.
 * Format: one entry per line, fields tab-separated.
 * Entry format: TYPE\tkey\tvalue
 * Types: K (KeyExists), VE (ValueExact), VC (ValueContains), VR (ValueRegex)
 */
object HeaderMatcherSerializer {

    private const val ENTRY_SEP = "\n"
    private const val FIELD_SEP = "\t"

    fun serialize(matchers: List<HeaderMatcher>): String {
        return matchers.joinToString(ENTRY_SEP) { m ->
            when (m) {
                is HeaderMatcher.KeyExists -> "K${FIELD_SEP}${m.key}${FIELD_SEP}"
                is HeaderMatcher.ValueExact -> "VE${FIELD_SEP}${m.key}${FIELD_SEP}${m.value}"
                is HeaderMatcher.ValueContains -> "VC${FIELD_SEP}${m.key}${FIELD_SEP}${m.value}"
                is HeaderMatcher.ValueRegex -> "VR${FIELD_SEP}${m.key}${FIELD_SEP}${m.pattern}"
            }
        }
    }

    fun deserialize(data: String): List<HeaderMatcher> {
        if (data.isBlank()) return emptyList()
        return data.split(ENTRY_SEP).mapNotNull { entry ->
            val parts = entry.split(FIELD_SEP)
            if (parts.size < 2) return@mapNotNull null
            val type = parts[0]
            val key = parts[1]
            val value = parts.getOrElse(2) { "" }
            when (type) {
                "K" -> HeaderMatcher.KeyExists(key)
                "VE" -> HeaderMatcher.ValueExact(key, value)
                "VC" -> HeaderMatcher.ValueContains(key, value)
                "VR" -> HeaderMatcher.ValueRegex(key, value)
                else -> null
            }
        }
    }
}
