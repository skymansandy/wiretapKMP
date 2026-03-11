package dev.skymansandy.wiretap.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.skymansandy.wiretap.WiretapContextProvider

actual class DriverFactory {

    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = WiretapDatabase.Schema,
            context = WiretapContextProvider.context,
            name = "wiretap.db",
        )
    }
}
