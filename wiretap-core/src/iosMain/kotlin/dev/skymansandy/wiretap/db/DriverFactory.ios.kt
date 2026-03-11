package dev.skymansandy.wiretap.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(WiretapDatabase.Schema, "wiretap.db")
    }
}
