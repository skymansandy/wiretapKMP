/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.urlsession

import dev.skymansandy.wiretap.domain.model.config.WiretapConfig
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.NSURLSessionDataTask

/**
 * Drop-in URLSession wrapper with Wiretap network inspection.
 *
 * Provides the same data-task API as NSURLSession — create once, use everywhere:
 * ```swift
 * let session = WiretapURLSession { config in
 *     #if DEBUG
 *     config.enabled = true
 *     #else
 *     config.enabled = false
 *     #endif
 *     config.logRetention = LogRetentionDays(days: 7)
 * }
 *
 * // Use like URLSession — full body, cancellation, and timing capture
 * session.dataTask(request: request) { data, response, error in ... }.resume()
 * ```
 *
 * When [WiretapConfig.enabled] is false, all methods pass through to the
 * underlying NSURLSession with zero interception overhead.
 *
 * APIs:
 * - [dataTask]: Returns an NSURLSessionDataTask with logging — caller must call resume().
 * - [intercept]: Fire-and-forget execution with full mock/throttle rule support.
 */
class WiretapURLSession(
    configuration: NSURLSessionConfiguration = NSURLSessionConfiguration.defaultSessionConfiguration,
    configure: WiretapConfig.() -> Unit = {},
) {

    private val session: NSURLSession = NSURLSession.sessionWithConfiguration(
        configuration,
        null,
        null,
    )

    private val interceptor = WiretapURLSessionInterceptor(
        session = session,
        configure = configure,
    )

    /**
     * Creates a data task with Wiretap logging. Returns the task — caller must call resume().
     * Captures full request/response body, headers, timing, and cancellation.
     * Mock/throttle rules are NOT applied. Use [intercept] for full rule support.
     */
    fun dataTask(
        request: NSURLRequest,
        completionHandler: (NSData?, NSURLResponse?, NSError?) -> Unit,
    ): NSURLSessionDataTask = interceptor.dataTask(request, completionHandler)

    /**
     * Creates a data task for a URL string with Wiretap logging.
     */
    fun dataTask(
        url: String,
        completionHandler: (NSData?, NSURLResponse?, NSError?) -> Unit,
    ): NSURLSessionDataTask = interceptor.dataTask(url, completionHandler)

    /**
     * Creates a data task for a URL with Wiretap logging.
     */
    fun dataTask(
        url: NSURL,
        completionHandler: (NSData?, NSURLResponse?, NSError?) -> Unit,
    ): NSURLSessionDataTask {
        val request = NSURLRequest.requestWithURL(url)
        return interceptor.dataTask(request, completionHandler)
    }

    /**
     * Intercepts a URL request with full rule support (mock, throttle).
     * Automatically executes the request and calls [completionHandler] with the result.
     * For mock rules, the completion handler is called with mock data without network access.
     */
    fun intercept(
        request: NSURLRequest,
        completionHandler: (NSData?, NSHTTPURLResponse?, NSError?) -> Unit,
    ) = interceptor.intercept(request, completionHandler)

    /**
     * Invalidates the session, cancelling all outstanding tasks.
     */
    fun invalidateAndCancel() {
        session.invalidateAndCancel()
    }

    /**
     * Allows outstanding tasks to finish, then invalidates the session.
     */
    fun finishTasksAndInvalidate() {
        session.finishTasksAndInvalidate()
    }
}
