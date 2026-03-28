package dev.skymansandy.wiretap.okhttp

import dev.skymansandy.wiretap.domain.model.config.WiretapConfig
import okhttp3.EventListener
import okhttp3.Interceptor
import okhttp3.Response

@Suppress("UnusedPrivateProperty")
class WiretapOkHttpInterceptor(
    configure: WiretapConfig.() -> Unit = {},
) : Interceptor {

    val eventListenerFactory: EventListener.Factory = EventListener.Factory {
        EventListener.NONE
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}
