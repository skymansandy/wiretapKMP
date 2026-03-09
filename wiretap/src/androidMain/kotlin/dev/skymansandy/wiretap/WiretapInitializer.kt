package dev.skymansandy.wiretap

import android.content.Context
import androidx.startup.Initializer

class WiretapInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        WiretapContextProvider.init(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
