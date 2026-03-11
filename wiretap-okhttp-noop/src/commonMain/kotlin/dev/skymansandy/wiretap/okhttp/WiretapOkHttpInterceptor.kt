package dev.skymansandy.wiretap.okhttp

import okhttp3.Interceptor
import okhttp3.Response

class WiretapOkHttpInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request())
}
