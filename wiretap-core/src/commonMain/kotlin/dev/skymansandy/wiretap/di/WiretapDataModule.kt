package dev.skymansandy.wiretap.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.skymansandy.wiretap.data.db.room.WiretapRoomDatabase
import dev.skymansandy.wiretap.data.db.room.createWiretapDatabaseBuilder
import dev.skymansandy.wiretap.data.repository.HttpRepositoryImpl
import dev.skymansandy.wiretap.data.repository.RuleRepositoryImpl
import dev.skymansandy.wiretap.data.repository.SocketRepositoryImpl
import dev.skymansandy.wiretap.domain.repository.HttpRepository
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.domain.repository.SocketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.koin.dsl.module

val wiretapDataModule = module {

    single<WiretapRoomDatabase> {
        createWiretapDatabaseBuilder()
            .fallbackToDestructiveMigration(true)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
            .also { db ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.httpRoomDao().closeStaleHttpLogs()
                    db.socketRoomDao().closeStaleSocketLogs()
                }
            }
    }

    single<HttpRepository> {
        HttpRepositoryImpl(httpRoomDao = get<WiretapRoomDatabase>().httpRoomDao())
    }

    single<RuleRepository> {
        RuleRepositoryImpl(ruleRoomDao = get<WiretapRoomDatabase>().ruleRoomDao())
    }

    single<SocketRepository> {
        SocketRepositoryImpl(socketRoomDao = get<WiretapRoomDatabase>().socketRoomDao())
    }
}
