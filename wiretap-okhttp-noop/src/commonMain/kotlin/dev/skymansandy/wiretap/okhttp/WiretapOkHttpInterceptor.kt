package dev.skymansandy.wiretap.okhttp

import dev.skymansandy.wiretap.config.WiretapConfig
import okhttp3.Interceptor
import okhttp3.Response

@Suppress("UnusedPrivateProperty")
class WiretapOkHttpInterceptor(
    configure: WiretapConfig.() -> Unit = {},
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request())
}
