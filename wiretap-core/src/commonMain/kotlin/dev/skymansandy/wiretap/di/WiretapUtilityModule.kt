package dev.skymansandy.wiretap.di

import dev.skymansandy.wiretap.helper.logger.WiretapLogger
import dev.skymansandy.wiretap.helper.logger.WiretapLoggerImpl
import org.koin.dsl.module

val wiretapUtilityModule = module {

    single<WiretapLogger> {
        WiretapLoggerImpl()
    }
}
