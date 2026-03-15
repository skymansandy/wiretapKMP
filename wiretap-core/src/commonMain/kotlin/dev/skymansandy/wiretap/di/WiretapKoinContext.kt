package dev.skymansandy.wiretap.di

import org.koin.core.Koin
import org.koin.dsl.koinApplication

internal object WiretapKoinContext {

    val koin: Koin by lazy {
        koinApplication {
            modules(wiretapModule)
        }.koin
    }
}
