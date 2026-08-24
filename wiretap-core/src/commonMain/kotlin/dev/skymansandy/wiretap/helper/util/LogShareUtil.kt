/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

internal const val SHARE_DIR_NAME = "wiretap_share"

/**
 * Roughly 400 KB once encoded as UTF-16, comfortably under the ~1 MB Binder
 * transaction budget an Android share intent has to fit inside.
 */
internal const val MAX_SHARE_TEXT_LENGTH = 200_000

internal expect fun shareLogText(subject: String, text: String): String?

internal expect fun shareLogAsFile(content: String, fileName: String): String?

internal fun exceedsShareTextLimit(text: String): Boolean = text.length > MAX_SHARE_TEXT_LENGTH

/**
 * Names a share file per log entry rather than per log type.
 *
 * The id keeps the name unique without needing to be sanitised, which matters
 * because WiretapFileProvider resolves a request by its last path segment.
 */
internal fun shareFileName(prefix: String, id: Long): String = "${prefix}_$id.txt"

/**
 * Shares [text] inline where it fits, and hands off to a file when it does not.
 *
 * A socket or SSE transcript has no upper bound, and putting one in an intent
 * extra past the Binder limit takes the host app down with a
 * TransactionTooLargeException. Returns a message to surface when the fallback
 * kicks in, so the switch is visible rather than silent.
 */
internal fun shareLogTextOrFile(subject: String, text: String, fileName: String): String? =
    if (exceedsShareTextLimit(text)) {
        shareLogAsFile(text, fileName)
        "Log too large to share as text \u2014 shared as a file instead"
    } else {
        shareLogText(subject, text)
    }
