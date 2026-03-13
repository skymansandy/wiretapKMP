package dev.skymansandy.wiretap.di

import dev.skymansandy.wiretap.config.WiretapConfig
import dev.skymansandy.wiretap.data.db.dao.NetworkDao
import dev.skymansandy.wiretap.data.db.dao.NetworkDaoImpl
import dev.skymansandy.wiretap.data.db.dao.RuleDao
import dev.skymansandy.wiretap.data.db.dao.RuleDaoImpl
import dev.skymansandy.wiretap.data.db.driver.DriverFactory
import dev.skymansandy.wiretap.data.repository.NetworkRepositoryImpl
import dev.skymansandy.wiretap.data.repository.RuleRepositoryImpl
import dev.skymansandy.wiretap.db.WiretapDatabase
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.domain.orchestrator.WiretapOrchestratorImpl
import dev.skymansandy.wiretap.domain.repository.NetworkRepository
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.helper.logger.NetworkLogger
import dev.skymansandy.wiretap.helper.logger.NetworkLoggerImpl
import org.koin.dsl.module

val wiretapModule = module {
    single<WiretapConfig> { WiretapConfig() }

    single<WiretapDatabase> {
        WiretapDatabase(DriverFactory().createDriver())
    }

    single<NetworkDao> { NetworkDaoImpl(database = get()) }
    single<RuleDao> { RuleDaoImpl(database = get()) }

    single<NetworkRepository> { NetworkRepositoryImpl(networkDao = get()) }
    single<RuleRepository> { RuleRepositoryImpl(ruleDao = get()) }

    single<NetworkLogger> { NetworkLoggerImpl() }

    single<WiretapOrchestrator> {
        WiretapOrchestratorImpl(
            config = get(),
            networkRepository = get(),
            networkLogger = get(),
        )
    }
}
