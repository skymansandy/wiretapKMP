package dev.skymansandy.wiretap.okhttp.timing

import dev.skymansandy.wiretap.domain.model.TimingPhase

internal class WiretapTimingCollector {

    var dnsStartNs: Long = 0L
    var dnsEndNs: Long = 0L
    var connectStartNs: Long = 0L
    var connectEndNs: Long = 0L
    var secureConnectStartNs: Long = 0L
    var secureConnectEndNs: Long = 0L
    var requestHeadersStartNs: Long = 0L
    var requestBodyEndNs: Long = 0L
    var responseHeadersStartNs: Long = 0L
    var responseHeadersEndNs: Long = 0L
    var responseBodyStartNs: Long = 0L
    var responseBodyEndNs: Long = 0L

    @Suppress("CyclomaticComplexMethod")
    fun toTimingPhases(callStartNs: Long): List<TimingPhase> = buildList {
        fun offsetMs(ns: Long): Double = (ns - callStartNs) / 1_000_000.0
        fun durationMs(startNs: Long, endNs: Long): Double = (endNs - startNs) / 1_000_000.0

        if (dnsStartNs > 0 && dnsEndNs > 0) {
            add(
                TimingPhase(
                    name = "DNS",
                    startMs = offsetMs(dnsStartNs),
                    durationMs = durationMs(dnsStartNs, dnsEndNs),
                ),
            )
        }

        if (connectStartNs > 0 && connectEndNs > 0) {
            add(
                TimingPhase(
                    name = "TCP",
                    startMs = offsetMs(connectStartNs),
                    durationMs = durationMs(connectStartNs, connectEndNs),
                ),
            )
        }

        if (secureConnectStartNs > 0 && secureConnectEndNs > 0) {
            add(
                TimingPhase(
                    name = "TLS",
                    startMs = offsetMs(secureConnectStartNs),
                    durationMs = durationMs(secureConnectStartNs, secureConnectEndNs),
                ),
            )
        }

        val reqStart = if (requestHeadersStartNs > 0) requestHeadersStartNs else 0L
        val reqEnd = if (requestBodyEndNs > 0) requestBodyEndNs else reqStart
        if (reqStart > 0) {
            add(
                TimingPhase(
                    name = "Request",
                    startMs = offsetMs(reqStart),
                    durationMs = durationMs(reqStart, reqEnd),
                ),
            )
        }

        if (responseHeadersStartNs > 0 && responseHeadersEndNs > 0) {
            add(
                TimingPhase(
                    name = "Waiting",
                    startMs = offsetMs(responseHeadersStartNs),
                    durationMs = durationMs(responseHeadersStartNs, responseHeadersEndNs),
                ),
            )
        }

        if (responseBodyStartNs > 0 && responseBodyEndNs > 0) {
            add(
                TimingPhase(
                    name = "Download",
                    startMs = offsetMs(responseBodyStartNs),
                    durationMs = durationMs(responseBodyStartNs, responseBodyEndNs),
                ),
            )
        }
    }
}
