package dev.skymansandy.wiretap.helper.util

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

internal actual fun copyToClipboard(text: String) {
    val selection = StringSelection(text)
    Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
}
