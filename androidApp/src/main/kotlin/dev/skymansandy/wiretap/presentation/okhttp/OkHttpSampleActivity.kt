package dev.skymansandy.wiretap.presentation.okhttp

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.skymansandy.wiretap.di.okHttpSampleModule
import dev.skymansandy.wiretapsample.ui.SampleApp
import dev.skymansandy.wiretapsample.ui.theme.WiretapTheme
import org.koin.compose.KoinIsolatedContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinApplication

internal class OkHttpSampleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        setContent {
            KoinIsolatedContext(
                context = koinApplication {
                    modules(okHttpSampleModule)
                },
            ) {
                WiretapTheme {
                    SampleApp(
                        title = "OkHttp Sample",
                        httpActions = koinViewModel<OkHttpViewModel>(),
                        wsActions = koinViewModel<OkHttpWsViewModel>(),
                    )
                }
            }
        }
    }
}
