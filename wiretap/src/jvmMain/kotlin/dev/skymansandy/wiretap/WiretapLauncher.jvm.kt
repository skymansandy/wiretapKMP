package dev.skymansandy.wiretap

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.awt.ComposePanel
import dev.skymansandy.wiretap.ui.WiretapScreen
import java.awt.Dimension
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
