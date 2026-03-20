package dev.skymansandy.wiretap.presentation.okhttp

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val okHttpSampleModule = module {

    single { createOkHttpClient() }

    viewModelOf(::OkHttpViewModel)

    viewModelOf(::OkHttpWsViewModel)
}
