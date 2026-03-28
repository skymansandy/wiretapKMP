package dev.skymansandy.wiretap.di

import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import kotlin.concurrent.Volatile

internal object WiretapKoinContext {

    @Volatile
    private var testKoin: Koin? = null

    val koin: Koin get() = testKoin ?: productionApp.koin

    val productionApp: KoinApplication by lazy {
        koinApplication {
            modules(wiretapModule)
        }
    }

    fun setTestKoin(koin: Koin?) {
        testKoin = koin
    }
}
