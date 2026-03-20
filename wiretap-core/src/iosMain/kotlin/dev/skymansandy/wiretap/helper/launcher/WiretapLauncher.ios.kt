package dev.skymansandy.wiretap.helper.launcher

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.ComposeUIViewController
import dev.skymansandy.wiretap.shake.ShakeDetector
import dev.skymansandy.wiretap.ui.WiretapScreen
import platform.UIKit.UIApplication
import platform.UIKit.UIModalPresentationFullScreen
import platform.UIKit.UINavigationController
import platform.UIKit.UITabBarController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

private var wiretapViewControllerInstance: UIViewController? = null

fun WiretapViewController(): UIViewController {
    return ComposeUIViewController {
        MaterialTheme {
            WiretapScreen(onBack = {
                getTopMostViewController()?.dismissViewControllerAnimated(true, completion = null)
                wiretapViewControllerInstance = null
            })
        }
    }.also {
        wiretapViewControllerInstance = it
    }
}

actual fun startWiretap() {
    if (wiretapViewControllerInstance != null) return
    val topVc = getTopMostViewController() ?: return
    val wiretapVc = WiretapViewController()
    wiretapVc.setModalPresentationStyle(UIModalPresentationFullScreen)
    topVc.presentViewController(wiretapVc, animated = true, completion = null)
}

actual fun enableWiretapLauncher() {
    ShakeDetector.enable {
        if (wiretapViewControllerInstance != null) return@enable
        startWiretap()
    }
}

private fun getTopMostViewController(
    base: UIViewController? = UIApplication.sharedApplication.topWindow?.rootViewController
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

private val UIApplication.topWindow: UIWindow?
    get() {
        return connectedScenes
            .asSequence()
            .mapNotNull { it as? UIWindowScene }
            .flatMap { it.windows.asSequence() }
            .filterIsInstance<UIWindow>()
            .lastOrNull { it.isKeyWindow() }
    }
