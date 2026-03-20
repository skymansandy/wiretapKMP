package dev.skymansandy.wiretap.di

import org.koin.core.Koin
import org.koin.dsl.koinApplication
import kotlin.concurrent.Volatile

internal object WiretapKoinContext {

    @Volatile
    private var testKoin: Koin? = null

    val koin: Koin get() = testKoin ?: productionKoin

    private val productionKoin: Koin by lazy {
        koinApplication {
            modules(wiretapModule)
        }.koin
    }

    fun setTestKoin(koin: Koin?) {
        testKoin = koin
    }
}
