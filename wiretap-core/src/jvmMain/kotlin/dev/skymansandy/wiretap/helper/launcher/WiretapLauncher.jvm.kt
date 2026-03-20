package dev.skymansandy.wiretap.helper.launcher

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.awt.ComposePanel
import dev.skymansandy.wiretap.ui.WiretapScreen
import java.awt.Dimension
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent
import javax.swing.JFrame
import javax.swing.SwingUtilities

actual fun startWiretap() {
    SwingUtilities.invokeLater {
        val frame = JFrame("Wiretap").apply {
            defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            size = Dimension(480, 800)

            val composePanel = ComposePanel()
            composePanel.setContent {
                MaterialTheme {
                    WiretapScreen(onBack = { dispose() })
                }
            }
            contentPane.add(composePanel)
        }
        frame.isVisible = true
    }
}

actual fun enableWiretapLauncher() {
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher { event ->
        if (event.id == KeyEvent.KEY_PRESSED &&
            event.modifiersEx == (KeyEvent.CTRL_DOWN_MASK or KeyEvent.SHIFT_DOWN_MASK) &&
            event.keyCode == KeyEvent.VK_D
        ) {
            startWiretap()
            true
        } else {
            false
        }
    }
}
