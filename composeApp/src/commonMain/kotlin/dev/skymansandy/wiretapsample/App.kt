package dev.skymansandy.wiretapsample

import androidx.compose.runtime.Composable
import dev.skymansandy.wiretapsample.di.sampleAppModule
import dev.skymansandy.wiretapsample.ui.SampleApp
import dev.skymansandy.wiretapsample.viewmodel.KtorSampleViewModel
import dev.skymansandy.wiretapsample.viewmodel.KtorWebSocketViewModel
import org.koin.compose.KoinIsolatedContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinApplication

@Composable
fun App(title: String = "") {
    KoinIsolatedContext(
        context = koinApplication {
            modules(sampleAppModule)
        },
    ) {
        SampleApp(
            title = title,
            httpActions = koinViewModel<KtorSampleViewModel>(),
            wsActions = koinViewModel<KtorWebSocketViewModel>(),
        )
    }
}
