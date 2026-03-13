package dev.skymansandy.wiretapsample.di

import dev.skymansandy.wiretap.di.WiretapDi
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initKoin(): KoinApplication {
    return startKoin {
        modules(WiretapDi.koinModule)
    }
}
