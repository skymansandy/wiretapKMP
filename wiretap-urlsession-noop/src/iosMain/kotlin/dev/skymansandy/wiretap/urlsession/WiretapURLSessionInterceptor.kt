package dev.skymansandy.wiretap.urlsession

import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLResponse
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionDataTask
import platform.Foundation.NSURL
import platform.Foundation.dataTaskWithRequest

/**
 * No-op URLSession interceptor for release builds.
 * Passes requests through without logging.
 */
class WiretapURLSessionInterceptor(
    private val session: NSURLSession = NSURLSession.sharedSession,
) {

    companion object {
        val shared by lazy { WiretapURLSessionInterceptor() }
    }

    fun intercept(
        request: NSURLRequest,
        completionHandler: (NSData?, NSHTTPURLResponse?, NSError?) -> Unit,
    ) {
        session.dataTaskWithRequest(request) { data, response, error ->
            completionHandler(data, response as? NSHTTPURLResponse, error)
        }.resume()
    }

    fun dataTask(
        request: NSURLRequest,
        completionHandler: (NSData?, NSURLResponse?, NSError?) -> Unit,
    ): NSURLSessionDataTask = session.dataTaskWithRequest(request, completionHandler)

    fun dataTask(
        url: String,
        completionHandler: (NSData?, NSURLResponse?, NSError?) -> Unit,
    ): NSURLSessionDataTask {
        val nsUrl = NSURL(string = url)
        val request = NSURLRequest(uRL = nsUrl)
        return dataTask(request, completionHandler)
    }
}
