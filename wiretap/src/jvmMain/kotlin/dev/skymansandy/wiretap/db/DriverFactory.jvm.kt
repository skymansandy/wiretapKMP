package dev.skymansandy.wiretap.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:wiretap.db")
        WiretapDatabase.Schema.create(driver)
        return driver
    }
}
