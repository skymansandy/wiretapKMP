package dev.skymansandy.wiretap.data.db.driver

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.skymansandy.wiretap.db.WiretapDatabase
import dev.skymansandy.wiretap.helper.initializer.WiretapContextProvider

actual class DriverFactory {

    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = WiretapDatabase.Companion.Schema,
            context = WiretapContextProvider.context,
            name = "wiretap.db",
        )
    }
}
