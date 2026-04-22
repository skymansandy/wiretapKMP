@file:Suppress("UnusedParameter")

/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.okhttp.sse

import dev.skymansandy.wiretap.domain.model.config.sse.WiretapSseConfig
import dev.skymansandy.wiretap.helper.markers.ExperimentalWiretapSseApi
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener

/**
 * No-op EventSource listener for release builds.
 * Pure pass-through to the delegate listener.
 */
@ExperimentalWiretapSseApi
internal class WiretapOkHttpEventSourceListener(
    private val delegate: EventSourceListener,
) : EventSourceListener() {

    override fun onOpen(eventSource: EventSource, response: Response) {
        delegate.onOpen(eventSource, response)
    }

    override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
        delegate.onEvent(eventSource, id, type, data)
    }

    override fun onClosed(eventSource: EventSource) {
        delegate.onClosed(eventSource)
    }

    override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
        delegate.onFailure(eventSource, t, response)
    }
}

/**
 * No-op: returns the listener wrapped in [WiretapOkHttpEventSourceListener] for API parity.
 */
@ExperimentalWiretapSseApi
@Deprecated(
    message = "Use wiretapped() with config builder instead.",
    replaceWith = ReplaceWith("wiretapped { enabled = true }"),
)
fun EventSourceListener.wiretapped(
    enabled: Boolean = true,
): EventSourceListener = WiretapOkHttpEventSourceListener(this)

@ExperimentalWiretapSseApi
fun EventSourceListener.wiretapped(
    configure: WiretapSseConfig.() -> Unit,
): EventSourceListener = WiretapOkHttpEventSourceListener(this)
