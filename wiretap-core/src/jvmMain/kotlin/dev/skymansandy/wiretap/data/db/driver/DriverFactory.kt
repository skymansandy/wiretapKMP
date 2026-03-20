package dev.skymansandy.wiretap.data.db.driver

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.skymansandy.wiretap.db.WiretapDatabase

internal actual class DriverFactory {

    actual fun createDriver(): SqlDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:wiretap.db")
        migrateOrRecreate(driver)
        return driver
    }

    private fun migrateOrRecreate(driver: SqlDriver) {
        val schema = WiretapDatabase.Schema
        val currentVersion = try {
            driver.executeQuery(
                null,
                "PRAGMA user_version",
                mapper = { cursor ->
                    cursor.next()
                    QueryResult.Value(cursor.getLong(0) ?: 0L)
                },
                parameters = 0,
            ).value
        } catch (_: Exception) {
            0L
        }

        if (currentVersion == 0L) {
            schema.create(driver)
            driver.execute(null, "PRAGMA user_version = ${schema.version}", 0)
        } else if (currentVersion < schema.version) {
            driver.execute(null, "PRAGMA writable_schema = ON", 0)
            driver.execute(null, "DELETE FROM sqlite_master", 0)
            driver.execute(null, "PRAGMA writable_schema = OFF", 0)
            driver.execute(null, "VACUUM", 0)
            schema.create(driver)
            driver.execute(null, "PRAGMA user_version = ${schema.version}", 0)
        }
    }
}
