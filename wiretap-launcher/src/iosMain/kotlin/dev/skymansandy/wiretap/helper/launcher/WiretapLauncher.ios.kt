/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.launcher

import androidx.compose.ui.window.ComposeUIViewController
import dev.skymansandy.wiretap.shake.ShakeDetector
import dev.skymansandy.wiretap.ui.screens.WiretapConsole
import dev.skymansandy.wiretap.ui.theme.WiretapTheme
import platform.UIKit.UIAdaptivePresentationControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UINavigationController
import platform.UIKit.UIPresentationController
import platform.UIKit.UITabBarController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.presentationController
import platform.darwin.NSObject

private var wiretapViewControllerInstance: UIViewController? = null

/**
 * Clears the cached instance when the console is dismissed by the sheet's
 * swipe-down gesture rather than by our own close handler. Without this the
 * cached reference goes stale and [launchWiretapConsole] would refuse to ever
 * present the console again.
 *
 * Must be a class, not a Kotlin `object`: Kotlin/Native cannot lower a singleton
 * that subclasses an Obj-C class. UIKit holds presentation delegates weakly, so the
 * single instance is kept alive by the top-level property below.
 */
private class WiretapDismissDelegate :
    NSObject(),
    UIAdaptivePresentationControllerDelegateProtocol {

    override fun presentationControllerDidDismiss(
        presentationController: UIPresentationController,
    ) {
        wiretapViewControllerInstance = null
    }
}

private val wiretapDismissDelegate = WiretapDismissDelegate()

@Suppress("FunctionNaming")
fun WiretapViewController(): UIViewController {
    return ComposeUIViewController {
        WiretapTheme {
            WiretapConsole(
                onBack = {
                    wiretapViewControllerInstance = null
                    getTopMostViewController()?.dismissViewControllerAnimated(
                        true,
                        completion = null,
                    )
                },
            )
        }
    }.also {
        wiretapViewControllerInstance = it
    }
}

actual fun launchWiretapConsole() {
    if (wiretapViewControllerInstance != null) return

    val topVc = getTopMostViewController() ?: return
    val wiretapVc = WiretapViewController()
    // Page sheet rather than full screen: it gives the console the native grabber
    // and swipe-down-to-dismiss iOS users expect from a modal.
    wiretapVc.setModalPresentationStyle(UIModalPresentationPageSheet)
    wiretapVc.presentationController?.setDelegate(wiretapDismissDelegate)
    topVc.presentViewController(wiretapVc, animated = true, completion = null)
}

actual fun enableWiretapLauncher() {
    ShakeDetector.enable {
        if (wiretapViewControllerInstance != null) return@enable

        launchWiretapConsole()
    }
}

internal fun getTopMostViewController(
    base: UIViewController? = UIApplication.sharedApplication.topWindow?.rootViewController,
): UIViewController? {
    if (base == null) return null

    return when (base) {
        is UINavigationController -> getTopMostViewController(base.visibleViewController)

        is UITabBarController -> base.selectedViewController?.let { getTopMostViewController(it) }

        else -> {
            if (base.presentedViewController != null) {
                getTopMostViewController(base.presentedViewController)
            } else {
                base
            }
        }
    }
}

internal val UIApplication.topWindow: UIWindow?
    get() = connectedScenes
        .asSequence()
        .mapNotNull { it as? UIWindowScene }
        .flatMap { it.windows.asSequence() }
        .filterIsInstance<UIWindow>()
        .lastOrNull { it.isKeyWindow() }
