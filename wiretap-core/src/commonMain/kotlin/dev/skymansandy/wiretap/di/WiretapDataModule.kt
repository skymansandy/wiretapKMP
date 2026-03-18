package dev.skymansandy.wiretap.di

import app.cash.sqldelight.db.SqlDriver
import dev.skymansandy.wiretap.config.WiretapConfig
import dev.skymansandy.wiretap.data.db.dao.NetworkDao
import dev.skymansandy.wiretap.data.db.dao.NetworkDaoImpl
import dev.skymansandy.wiretap.data.db.dao.RuleDao
import dev.skymansandy.wiretap.data.db.dao.RuleDaoImpl
import dev.skymansandy.wiretap.data.db.dao.SocketDao
import dev.skymansandy.wiretap.data.db.dao.SocketDaoImpl
import dev.skymansandy.wiretap.data.db.driver.DriverFactory
import dev.skymansandy.wiretap.data.repository.NetworkRepositoryImpl
import dev.skymansandy.wiretap.data.repository.RuleRepositoryImpl
import dev.skymansandy.wiretap.data.repository.SocketRepositoryImpl
import dev.skymansandy.wiretap.db.WiretapDatabase
import dev.skymansandy.wiretap.domain.repository.NetworkRepository
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.domain.repository.SocketRepository
import org.koin.dsl.module

val wiretapDataModule = module {

    single<WiretapConfig> {
        WiretapConfig()
    }

    single<SqlDriver> {
        DriverFactory().createDriver()
    }

    single<WiretapDatabase> {
        WiretapDatabase(
            driver = get(),
        )
    }

    single<NetworkDao> {
        NetworkDaoImpl(database = get())
    }

    single<RuleDao> {
        RuleDaoImpl(database = get())
    }

    single<SocketDao> {
        SocketDaoImpl(database = get())
    }

    single<NetworkRepository> {
        NetworkRepositoryImpl(networkDao = get())
    }

    single<RuleRepository> {
        RuleRepositoryImpl(ruleDao = get())
    }

    single<SocketRepository> {
        SocketRepositoryImpl(socketDao = get())
    }
}
