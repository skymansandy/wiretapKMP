/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

internal const val SHARE_DIR_NAME = "wiretap_share"

internal expect fun shareLogText(subject: String, text: String): String?

internal expect fun shareLogAsFile(content: String, fileName: String): String?
