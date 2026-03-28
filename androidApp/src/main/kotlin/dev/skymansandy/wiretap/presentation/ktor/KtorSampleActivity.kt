package dev.skymansandy.wiretap.presentation.ktor

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.skymansandy.wiretapsample.App
import dev.skymansandy.wiretapsample.ui.theme.WiretapTheme

internal class KtorSampleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        setContent {
            WiretapTheme {
                App(title = "Ktor Sample")
            }
        }
    }
}
