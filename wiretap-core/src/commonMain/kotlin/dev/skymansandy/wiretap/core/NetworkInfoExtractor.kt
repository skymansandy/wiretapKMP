package dev.skymansandy.wiretap.core

import dev.skymansandy.wiretap.core.model.NetworkInfo

internal expect fun buildNetworkInfo(requestUrl: String, httpVersion: String): NetworkInfo