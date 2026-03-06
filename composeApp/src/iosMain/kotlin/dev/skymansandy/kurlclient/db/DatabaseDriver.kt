package dev.skymansandy.kurlclient.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual fun createDatabaseDriver(): SqlDriver =
    NativeSqliteDriver(KurlDatabase.Schema, "kurl.db")