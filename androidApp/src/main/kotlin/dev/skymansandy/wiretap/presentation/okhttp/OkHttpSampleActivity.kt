package dev.skymansandy.wiretap.presentation.okhttp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.skymansandy.wiretapsample.ui.SampleApp
import org.koin.compose.KoinIsolatedContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinApplication

internal class OkHttpSampleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KoinIsolatedContext(
                context = koinApplication {
                    modules(okHttpSampleModule)
                },
            ) {
                SampleApp(
                    title = "OkHttp Sample",
                    httpActions = koinViewModel<OkHttpViewModel>(),
                    wsActions = koinViewModel<OkHttpWsViewModel>(),
                )
            }
        }
    }
}
