package dev.skymansandy.wiretap.helper.initializer

import android.content.Context

internal object WiretapContextProvider {
    lateinit var context: Context
        private set

    fun init(context: Context) {
        this.context = context.applicationContext
    }
}
