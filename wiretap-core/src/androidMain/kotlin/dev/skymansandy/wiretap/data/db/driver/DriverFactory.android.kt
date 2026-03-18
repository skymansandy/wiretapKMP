package dev.skymansandy.wiretap.data.db.driver

import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.skymansandy.wiretap.db.WiretapDatabase
import dev.skymansandy.wiretap.helper.initializer.WiretapContextProvider

actual class DriverFactory {

    actual fun createDriver(): SqlDriver {
        val context = WiretapContextProvider.context
        val schema = WiretapDatabase.Companion.Schema

        return AndroidSqliteDriver(
            schema = schema,
            context = context,
            name = "wiretap.db",
            callback = object : AndroidSqliteDriver.Callback(schema) {
                override fun onCorruption(db: SupportSQLiteDatabase) {
                    context.deleteDatabase("wiretap.db")
                }

                override fun onDowngrade(
                    db: SupportSQLiteDatabase,
                    oldVersion: Int,
                    newVersion: Int
                ) {
                    context.deleteDatabase("wiretap.db")
                }
            },
        )
    }
}
