package dev.skymansandy.spektorsample.core

import dev.skymansandy.spektorsample.core.model.NetworkInfo

internal expect fun buildNetworkInfo(requestUrl: String, httpVersion: String): NetworkInfo