package dev.skymansandy.wiretap.di

import app.cash.sqldelight.db.SqlDriver
import dev.skymansandy.wiretap.data.db.dao.HttpDao
import dev.skymansandy.wiretap.data.db.dao.HttpDaoImpl
import dev.skymansandy.wiretap.data.db.dao.RuleDao
import dev.skymansandy.wiretap.data.db.dao.RuleDaoImpl
import dev.skymansandy.wiretap.data.db.dao.SocketDao
import dev.skymansandy.wiretap.data.db.dao.SocketDaoImpl
import dev.skymansandy.wiretap.data.db.driver.DriverFactory
import dev.skymansandy.wiretap.data.repository.HttpRepositoryImpl
import dev.skymansandy.wiretap.data.repository.RuleRepositoryImpl
import dev.skymansandy.wiretap.data.repository.SocketRepositoryImpl
import dev.skymansandy.wiretap.db.WiretapDatabase
import dev.skymansandy.wiretap.domain.repository.HttpRepository
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.domain.repository.SocketRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.koin.dsl.module

val wiretapDataModule = module {

    single<SqlDriver> {
        DriverFactory().createDriver()
    }

    single<WiretapDatabase> {
        WiretapDatabase(driver = get()).also { db ->
            CoroutineScope(Dispatchers.IO).launch {
                db.wiretapQueries.closeStaleHttpLogs()
                db.wiretapQueries.closeStaleSocketLogs()
            }
        }
    }

    single<HttpDao> {
        HttpDaoImpl(database = get())
    }

    single<RuleDao> {
        RuleDaoImpl(database = get())
    }

    single<SocketDao> {
        SocketDaoImpl(database = get())
    }

    single<HttpRepository> {
        HttpRepositoryImpl(httpDao = get())
    }

    single<RuleRepository> {
        RuleRepositoryImpl(ruleDao = get())
    }

    single<SocketRepository> {
        SocketRepositoryImpl(socketDao = get())
    }
}
