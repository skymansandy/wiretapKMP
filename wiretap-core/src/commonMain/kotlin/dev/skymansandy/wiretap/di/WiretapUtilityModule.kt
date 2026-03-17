package dev.skymansandy.wiretap.di

import dev.skymansandy.wiretap.helper.logger.NetworkLogger
import dev.skymansandy.wiretap.helper.logger.NetworkLoggerImpl
import org.koin.dsl.module

val wiretapUtilityModule = module {

    single<NetworkLogger> {
        NetworkLoggerImpl()
    }
}
