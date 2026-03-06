package dev.skymansandy.kurlclient.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

internal lateinit var appContext: Context

fun initAndroidContext(context: Context) {
    appContext = context.applicationContext
}

actual fun createDatabaseDriver(): SqlDriver =
    AndroidSqliteDriver(KurlDatabase.Schema, appContext, "kurl.db")