/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

private var shareWindow: UIWindow? = null

private fun presentShareSheet(items: List<Any>) {
    dispatch_async(dispatch_get_main_queue()) {
        val windowScene = UIApplication.sharedApplication.connectedScenes
            .asSequence()
            .mapNotNull { it as? UIWindowScene }
            .firstOrNull() ?: return@dispatch_async

        // Create a dedicated window to present the share sheet from
        val window = UIWindow(windowScene = windowScene)
        val rootVc = UIViewController()
        window.rootViewController = rootVc
        window.windowLevel = platform.UIKit.UIWindowLevelNormal + 1.0
        window.makeKeyAndVisible()
        shareWindow = window

        val activityVc = UIActivityViewController(
            activityItems = items,
            applicationActivities = null,
        )
        activityVc.completionWithItemsHandler = { _, _, _, _ ->
            shareWindow?.setHidden(true)
            shareWindow = null
        }
        rootVc.presentViewController(activityVc, animated = true, completion = null)
    }
}

internal actual fun shareHttpLogs(subject: String, text: String): String? {
    presentShareSheet(listOf(text))
    return null
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal actual fun shareHttpLogAsFile(content: String) {
    dispatch_async(dispatch_get_main_queue()) {
        try {
            val shareDir = NSTemporaryDirectory() + "$SHARE_DIR_NAME/"
            NSFileManager.defaultManager.createDirectoryAtPath(
                shareDir,
                withIntermediateDirectories = true,
                attributes = null,
                error = null,
            )

            val filePath = shareDir + HTTP_LOG_FILE_NAME
            val nsContent = NSString.create(string = content)
            val written = nsContent.writeToFile(
                filePath,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null,
            )
            if (!written) return@dispatch_async

            val fileUrl = NSURL.fileURLWithPath(filePath)
            presentShareSheet(listOf(fileUrl))
        } catch (_: Exception) {
            // Silently fail -- never crash the host app
        }
    }
}
