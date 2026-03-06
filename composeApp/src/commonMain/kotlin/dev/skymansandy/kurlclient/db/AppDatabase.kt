package dev.skymansandy.kurlclient.db

import app.cash.sqldelight.db.SqlDriver

object AppDatabase {
    private var _db: KurlDatabase? = null

    fun init(driver: SqlDriver) {
        if (_db == null) {
            _db = KurlDatabase(driver)
        }
    }

    val db: KurlDatabase
        get() = _db ?: error("AppDatabase not initialized. Call AppDatabase.init() at app startup.")
}

expect fun createDatabaseDriver(): SqlDriver