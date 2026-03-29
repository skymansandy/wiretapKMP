package dev.skymansandy.wiretap.di

import dev.skymansandy.wiretap.data.db.room.WiretapRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import kotlin.concurrent.Volatile

internal object WiretapKoinContext {

    @Volatile
    private var testKoin: Koin? = null

    val koin: Koin get() = testKoin ?: koinApp.koin

    val koinApp: KoinApplication by lazy {
        koinApplication {
            modules(wiretapModule)
        }.also { app ->
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                val db = app.koin.get<WiretapRoomDatabase>()
                db.httpRoomDao().closeStaleHttpLogs()
                db.socketRoomDao().closeStaleSocketLogs()
            }
        }
    }

    fun setTestKoin(koin: Koin?) {
        testKoin = koin
    }
}
