package dev.skymansandy.wiretap.di

import dev.skymansandy.wiretap.WiretapConfig
import dev.skymansandy.wiretap.dao.NetworkDao
import dev.skymansandy.wiretap.dao.NetworkDaoImpl
import dev.skymansandy.wiretap.dao.RuleDao
import dev.skymansandy.wiretap.dao.RuleDaoImpl
import dev.skymansandy.wiretap.db.DriverFactory
import dev.skymansandy.wiretap.db.WiretapDatabase
import dev.skymansandy.wiretap.engine.MockEngine
import dev.skymansandy.wiretap.engine.MockEngineImpl
import dev.skymansandy.wiretap.engine.RuleEngine
import dev.skymansandy.wiretap.engine.RuleEngineImpl
import dev.skymansandy.wiretap.engine.ThrottleEngine
import dev.skymansandy.wiretap.engine.ThrottleEngineImpl
import dev.skymansandy.wiretap.logger.NetworkLogger
import dev.skymansandy.wiretap.logger.NetworkLoggerImpl
import dev.skymansandy.wiretap.orchestrator.WiretapOrchestrator
import dev.skymansandy.wiretap.orchestrator.WiretapOrchestratorImpl
import dev.skymansandy.wiretap.repository.NetworkRepository
import dev.skymansandy.wiretap.repository.NetworkRepositoryImpl
import dev.skymansandy.wiretap.repository.RuleRepository
import dev.skymansandy.wiretap.repository.RuleRepositoryImpl
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

    single<MockEngine> { MockEngineImpl() }
    single<ThrottleEngine> { ThrottleEngineImpl() }
    single<RuleEngine> { RuleEngineImpl(ruleRepository = get(), mockEngine = get(), throttleEngine = get()) }

    single<NetworkLogger> { NetworkLoggerImpl() }

    single<WiretapOrchestrator> {
        WiretapOrchestratorImpl(
            config = get(),
            networkRepository = get(),
            networkLogger = get(),
        )
    }
}
