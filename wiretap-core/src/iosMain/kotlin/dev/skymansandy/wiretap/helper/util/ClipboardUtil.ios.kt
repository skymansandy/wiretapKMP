package dev.skymansandy.wiretap.helper.util

import platform.UIKit.UIPasteboard

internal actual fun copyToClipboard(text: String) {
    UIPasteboard.generalPasteboard.string = text
}
