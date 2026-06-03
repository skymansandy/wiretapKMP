/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.ws

import dev.skymansandy.wiretap.domain.model.config.ws.BinaryFrameDecoding
import dev.skymansandy.wiretap.helper.markers.InternalWiretapApi

/**
 * Renders a binary WebSocket payload as a display string according to [mode].
 *
 * Used by the Ktor and OkHttp WebSocket integrations to give libraries that
 * ship text-over-binary a readable log entry.
 */
@InternalWiretapApi
fun decodeBinaryFrame(bytes: ByteArray, mode: BinaryFrameDecoding): String = when (mode) {
    BinaryFrameDecoding.Placeholder -> binaryPlaceholder(bytes.size)
    BinaryFrameDecoding.Utf8 -> bytes.decodeToString()
    BinaryFrameDecoding.Auto -> tryDecodeAsText(bytes) ?: binaryPlaceholder(bytes.size)
    is BinaryFrameDecoding.Custom -> mode.decode(bytes)
}

private fun binaryPlaceholder(size: Int): String = "[Binary: $size bytes]"

private const val ALLOWED_CONTROL_CHARS = "\t\n\r"

private fun tryDecodeAsText(bytes: ByteArray): String? {
    if (bytes.isEmpty()) return ""

    val text = runCatching {
        bytes.decodeToString(throwOnInvalidSequence = true)
    }.getOrNull() ?: return null

    text.forEach { c ->
        if (c.code in 0..31 && c !in ALLOWED_CONTROL_CHARS) return null
    }

    return text
}
