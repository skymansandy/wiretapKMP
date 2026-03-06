package dev.skymansandy.kurlclient.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

actual fun createDatabaseDriver(): SqlDriver {
    val dbFile = File(System.getProperty("user.home"), ".kurlclient/kurl.db")
    dbFile.parentFile?.mkdirs()
    val isNew = !dbFile.exists()
    val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
    if (isNew) {
        KurlDatabase.Schema.create(driver)
    }
    return driver
}