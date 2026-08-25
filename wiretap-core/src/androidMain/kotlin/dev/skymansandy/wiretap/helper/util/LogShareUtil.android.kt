/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import android.content.Intent
import androidx.core.net.toUri
import dev.skymansandy.wiretap.helper.initializer.WiretapContextProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

internal actual fun shareLogText(subject: String, text: String): String? {
    val context = WiretapContextProvider.context
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }

    context.startActivity(
        Intent.createChooser(intent, "Share network log").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )
    return null
}

internal actual fun shareLogAsFile(content: String, fileName: String): String? {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val context = WiretapContextProvider.context
            val shareDir = File(context.cacheDir, SHARE_DIR_NAME).apply { mkdirs() }
            val file = File(shareDir, fileName)
            file.writeText(content, Charsets.UTF_8)

            val authority = "${context.packageName}.wiretap.fileprovider"
            val uri = "content://$authority/$fileName".toUri()

            withContext(Dispatchers.Main) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(
                    Intent.createChooser(intent, "Share network log").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    },
                )
            }
        } catch (_: Exception) {
            // Silently fail -- never crash the host app
        }
    }
    return null
}
