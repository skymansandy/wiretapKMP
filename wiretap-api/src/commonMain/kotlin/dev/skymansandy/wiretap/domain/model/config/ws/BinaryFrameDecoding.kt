/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.domain.model.config.ws

/**
 * Controls how WebSocket Binary frames are rendered in Wiretap logs.
 *
 * @see WiretapWsConfig.binaryDecoding
 */
sealed interface BinaryFrameDecoding {

    /**
     * Default. Attempt strict UTF-8 decode; if the payload is valid printable
     * text (no NUL, no unescaped control bytes), display it as text. Otherwise
     * fall back to `[Binary: N bytes]`.
     *
     * Covers libraries that ship text-over-binary (e.g. SignalRKore) without
     * misrepresenting genuine binary formats like protobuf or MessagePack.
     */
    object Auto : BinaryFrameDecoding

    /**
     * Always decode as UTF-8, even if the payload is not valid text. Invalid
     * sequences are replaced with the Unicode replacement character (U+FFFD).
     * Use when you know all binary frames on the wire are UTF-8 strings.
     */
    object Utf8 : BinaryFrameDecoding

    /**
     * Never decode. Always render `[Binary: N bytes]`. Use for high-volume
     * binary protocols where attempting to decode is just noise.
     */
    object Placeholder : BinaryFrameDecoding

    /**
     * Use a user-supplied function to render the payload. Useful for non-UTF-8
     * charsets, hex previews, MessagePack pretty-print, etc.
     *
     * **Avoid capturing UI state.** The Ktor plugin holds this lambda for the
     * lifetime of the [io.ktor.client.HttpClient], which is typically app-scoped;
     * a lambda that captures an `Activity` or composable scope will keep that
     * scope alive. Prefer a top-level function or an object method reference.
     */
    fun interface Custom : BinaryFrameDecoding {
        fun decode(bytes: ByteArray): String
    }
}
