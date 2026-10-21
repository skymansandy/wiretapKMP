/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import dev.skymansandy.wiretap.domain.model.HttpLog

internal const val HTTP_LOG_FILE_PREFIX = "wiretap_http_log"

internal fun buildShareText(entry: HttpLog): String = buildString {
    appendLine("${entry.method} ${entry.responseCode}")
    appendLine(entry.url)
    appendLine("Duration: ${entry.durationMs}ms | Source: ${entry.source.name}")
    entry.protocol?.let { appendLine("HTTP Version: $it") }
    entry.remoteAddress?.let { appendLine("Remote Address: $it") }
    entry.tlsProtocol?.let { appendLine("TLS Protocol: $it") }
    entry.cipherSuite?.let { appendLine("Cipher Suite: $it") }
    entry.certificateCn?.let { appendLine("Certificate CN: $it") }
    entry.issuerCn?.let { appendLine("Issuer CN: $it") }
    entry.certificateExpiry?.let { appendLine("Valid Until: $it") }
    appendLine()
    appendLine("--- Request Headers ---")
    if (entry.requestHeaders.isEmpty()) {
        appendLine("(none)")
    } else {
        entry.requestHeaders.forEach { (k, v) -> appendLine("$k: $v") }
    }
    appendLine()
    appendLine("--- Request Body ---")
    appendLine(entry.requestBody ?: "(none)")
    appendLine()
    appendLine("--- Response Headers ---")
    if (entry.responseHeaders.isEmpty()) {
        appendLine("(none)")
    } else {
        entry.responseHeaders.forEach { (k, v) -> appendLine("$k: $v") }
    }
    appendLine()
    appendLine("--- Response Body ---")
    append(entry.responseBody ?: "(none)")
}

internal fun buildCurlCommand(entry: HttpLog): String = buildString {
    append("curl -X ${entry.method} ${entry.url.shellQuoted()}")
    entry.requestHeaders.forEach { (k, v) ->
        append(" \\\n  -H ${"$k: $v".shellQuoted()}")
    }
    val body = entry.requestBody
    if (!body.isNullOrEmpty()) {
        append(" \\\n  --data-raw ${minifyJson(body).shellQuoted()}")
    }
}

/**
 * Wraps a value in single quotes, closing and reopening the quote around any literal
 * single quote so the command survives both a real shell and importers like Postman.
 */
private fun String.shellQuoted(): String = "'" + replace("'", "'\\''") + "'"
