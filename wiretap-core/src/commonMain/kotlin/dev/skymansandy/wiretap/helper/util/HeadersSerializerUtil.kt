/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

object HeadersSerializerUtil {

    fun serialize(headers: Map<String, String>): String {
        if (headers.isEmpty()) return ""
        return headers.entries.joinToString("\n") { "${it.key}: ${it.value}" }
    }

    fun deserialize(raw: String): Map<String, String> {
        if (raw.isBlank()) return emptyMap()
        return raw.lines()
            .filter { it.contains(":") }
            .associate { line ->
                val idx = line.indexOf(':')
                line.substring(0, idx).trim() to line.substring(idx + 1).trim()
            }
    }
}
