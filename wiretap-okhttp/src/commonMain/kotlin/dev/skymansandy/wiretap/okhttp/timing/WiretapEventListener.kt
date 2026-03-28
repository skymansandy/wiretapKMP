package dev.skymansandy.wiretap.okhttp.timing

import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Handshake
import okhttp3.Protocol
import okhttp3.Response
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.ConcurrentHashMap

internal class WiretapEventListener(
    private val collector: WiretapTimingCollector,
) : EventListener() {

    override fun dnsStart(call: Call, domainName: String) {
        collector.dnsStartNs = System.nanoTime()
    }

    override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
        collector.dnsEndNs = System.nanoTime()
    }

    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        collector.connectStartNs = System.nanoTime()
    }

    override fun secureConnectStart(call: Call) {
        collector.secureConnectStartNs = System.nanoTime()
    }

    override fun secureConnectEnd(call: Call, handshake: Handshake?) {
        collector.secureConnectEndNs = System.nanoTime()
    }

    override fun connectEnd(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy,
        protocol: Protocol?,
    ) {
        collector.connectEndNs = System.nanoTime()
    }

    override fun requestHeadersStart(call: Call) {
        collector.requestHeadersStartNs = System.nanoTime()
    }

    override fun requestBodyEnd(call: Call, byteCount: Long) {
        collector.requestBodyEndNs = System.nanoTime()
    }

    override fun responseHeadersStart(call: Call) {
        collector.responseHeadersStartNs = System.nanoTime()
    }

    override fun responseHeadersEnd(call: Call, response: Response) {
        collector.responseHeadersEndNs = System.nanoTime()
    }

    override fun responseBodyStart(call: Call) {
        collector.responseBodyStartNs = System.nanoTime()
    }

    override fun responseBodyEnd(call: Call, byteCount: Long) {
        collector.responseBodyEndNs = System.nanoTime()
    }
}

internal object WiretapTimingRegistry {

    private val collectors = ConcurrentHashMap<Call, WiretapTimingCollector>()

    fun create(call: Call): WiretapEventListener {
        val collector = WiretapTimingCollector()
        collectors[call] = collector
        return WiretapEventListener(collector)
    }

    fun retrieve(call: Call): WiretapTimingCollector? = collectors.remove(call)
}
