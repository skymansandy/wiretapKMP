package dev.skymansandy.wiretap.data.db.driver

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.skymansandy.wiretap.db.WiretapDatabase

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:wiretap.db")
        WiretapDatabase.Companion.Schema.create(driver)
        return driver
    }
}
