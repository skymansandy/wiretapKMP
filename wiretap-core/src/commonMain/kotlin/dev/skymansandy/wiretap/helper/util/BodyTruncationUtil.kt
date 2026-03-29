/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

/**
 * Truncates a body string to [maxLength] characters, appending a truncation notice.
 * Returns null if [maxLength] is 0 (body logging disabled).
 * Used internally by plugins before passing data to the orchestrator.
 */
fun String?.truncateBody(maxLength: Int): String? {
    if (maxLength == 0) return null
    if (this == null || length <= maxLength) return this
    return take(maxLength) + "\n\n--- [Wiretap] Body truncated (exceeded $maxLength chars) ---"
}
