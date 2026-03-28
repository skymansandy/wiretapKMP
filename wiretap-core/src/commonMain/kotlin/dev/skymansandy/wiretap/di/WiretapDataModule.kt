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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module

internal val wiretapDataModule = module {

    single<WiretapRoomDatabase> {
        createWiretapDatabaseBuilder()
            .fallbackToDestructiveMigration(true)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
            .also { db ->
                runBlocking(Dispatchers.IO) {
                    db.httpRoomDao().closeStaleHttpLogs()
                    db.socketRoomDao().closeStaleSocketLogs()
                }
            }
    }

    single<HttpRepository> {
        val db = get<WiretapRoomDatabase>()
        HttpRepositoryImpl(
            httpLogsDao = db.httpRoomDao(),
        )
    }

    single<RuleRepository> {
        val db = get<WiretapRoomDatabase>()
        RuleRepositoryImpl(
            rulesDao = db.ruleRoomDao(),
        )
    }

    single<SocketRepository> {
        val db = get<WiretapRoomDatabase>()
        SocketRepositoryImpl(
            socketLogsDao = db.socketRoomDao(),
        )
    }
}
