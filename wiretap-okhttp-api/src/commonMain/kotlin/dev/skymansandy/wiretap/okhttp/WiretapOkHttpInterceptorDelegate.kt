/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp

import dev.skymansandy.wiretap.domain.model.config.WiretapConfig
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Delegate interface for the OkHttp interceptor.
 *
 * Implemented by wiretap-okhttp's [RealOkHttpInterceptor] and registered in Koin.
 * The API module's [WiretapOkHttpInterceptor] resolves this at runtime — if found,
 * real logging/mocking activates; if not, requests pass through.
 */
interface WiretapOkHttpInterceptorDelegate {

    fun intercept(chain: Interceptor.Chain, config: WiretapConfig): Response
}
