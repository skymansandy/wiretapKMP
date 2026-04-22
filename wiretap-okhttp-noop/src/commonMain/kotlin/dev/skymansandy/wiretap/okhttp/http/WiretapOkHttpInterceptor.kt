/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp.http

import dev.skymansandy.wiretap.domain.model.config.http.WiretapHttpConfig
import okhttp3.Interceptor
import okhttp3.Response

@Suppress("UnusedPrivateProperty")
class WiretapOkHttpInterceptor(
    configure: WiretapHttpConfig.() -> Unit = {},
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}
