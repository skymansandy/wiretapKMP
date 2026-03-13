package dev.skymansandy.wiretap.data.db.driver

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import dev.skymansandy.wiretap.db.WiretapDatabase

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(WiretapDatabase.Companion.Schema, "wiretap.db")
    }
}
