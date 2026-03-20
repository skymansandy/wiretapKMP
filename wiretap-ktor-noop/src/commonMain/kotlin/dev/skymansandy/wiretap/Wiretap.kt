package dev.skymansandy.wiretap

import dev.skymansandy.wiretap.di.wiretapModule
import dev.skymansandy.wiretap.plugin.WiretapKtorPlugin

object Wiretap {

    val ktorPlugin get() = WiretapKtorPlugin

    val koinModule get() = wiretapModule
}
