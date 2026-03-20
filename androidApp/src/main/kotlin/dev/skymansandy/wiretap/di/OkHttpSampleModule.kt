package dev.skymansandy.wiretap.di

import dev.skymansandy.wiretap.presentation.okhttp.OkHttpViewModel
import dev.skymansandy.wiretap.presentation.okhttp.OkHttpWsViewModel
import dev.skymansandy.wiretap.presentation.okhttp.createOkHttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val okHttpSampleModule = module {

    single { createOkHttpClient() }

    viewModelOf(::OkHttpViewModel)

    viewModelOf(::OkHttpWsViewModel)
}
