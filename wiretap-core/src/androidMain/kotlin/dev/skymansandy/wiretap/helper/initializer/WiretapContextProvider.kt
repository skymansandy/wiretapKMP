package dev.skymansandy.wiretap.helper.initializer

import android.annotation.SuppressLint
import android.content.Context

internal object WiretapContextProvider {

    @SuppressLint("StaticFieldLeak")
    lateinit var context: Context
        private set

    fun init(context: Context) {
        this.context = context.applicationContext
    }
}
