/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp

import dev.skymansandy.wiretap.di.WiretapDi
import dev.skymansandy.wiretap.domain.model.config.WiretapConfig
import okhttp3.Interceptor
import okhttp3.Response
import org.koin.core.component.KoinComponent

/**
 * OkHttp interceptor for Wiretap network inspection.
 *
 * Configuration is applied via a builder lambda:
 * ```kotlin
 * OkHttpClient.Builder()
 *     .addInterceptor(WiretapOkHttpInterceptor {
 *         shouldLog = { url, _ -> url.contains("/api/") }
 *         headerAction = { key ->
 *             if (key.equals("Authorization", ignoreCase = true)) HeaderAction.Mask()
 *             else HeaderAction.Keep
 *         }
 *         logRetention = LogRetention.Days(7)
 *     })
 *     .build()
 * ```
 *
 * In debug builds (with wiretap-okhttp on the classpath), real logging activates.
 * In release builds (wiretap-okhttp-noop or API-only), requests pass through.
 */
class WiretapOkHttpInterceptor(
    configure: WiretapConfig.() -> Unit = {},
) : Interceptor {

    private val config = WiretapConfig().apply(configure)

    override fun intercept(chain: Interceptor.Chain): Response {
        val delegate = try {
            object : KoinComponent {
                override fun getKoin() = WiretapDi.getKoin()
            }.getKoin().getOrNull<WiretapOkHttpInterceptorDelegate>()
        } catch (_: Exception) {
            null
        }

        return delegate?.intercept(chain, config) ?: chain.proceed(chain.request())
    }
}
