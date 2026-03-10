package dev.skymansandy.wiretap

import android.app.Application
import dev.skymansandy.wiretapsample.di.initKoin

class WiretapSampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
