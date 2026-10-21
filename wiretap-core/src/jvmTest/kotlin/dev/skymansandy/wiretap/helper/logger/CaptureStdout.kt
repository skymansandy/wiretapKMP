/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.logger

import java.io.ByteArrayOutputStream
import java.io.PrintStream

/** Runs [block] with `System.out` redirected and returns everything it printed. */
internal fun captureStdout(block: () -> Unit): String {
    val original = System.out
    val buffer = ByteArrayOutputStream()
    System.setOut(PrintStream(buffer))
    try {
        block()
    } finally {
        System.setOut(original)
    }
    return buffer.toString()
}
