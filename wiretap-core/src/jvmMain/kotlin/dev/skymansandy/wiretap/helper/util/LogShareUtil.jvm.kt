/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File

internal actual fun shareLogText(subject: String, text: String): String? {
    return try {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(text), null)
        "Copied to clipboard"
    } catch (_: Exception) {
        null
    }
}

internal actual fun shareLogAsFile(content: String, fileName: String): String? {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val shareDir = File(System.getProperty("java.io.tmpdir"), SHARE_DIR_NAME).apply { mkdirs() }
            val file = File(shareDir, fileName)
            file.writeText(content, Charsets.UTF_8)

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file)
            }
        } catch (_: Exception) {
            // Silently fail -- never crash the host app
        }
    }
    return null
}
