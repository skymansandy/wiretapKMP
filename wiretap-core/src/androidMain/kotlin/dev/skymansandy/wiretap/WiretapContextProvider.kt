package dev.skymansandy.wiretap

import android.content.Context

internal object WiretapContextProvider {
    lateinit var context: Context
        private set

    fun init(context: Context) {
        this.context = context.applicationContext
    }
}
