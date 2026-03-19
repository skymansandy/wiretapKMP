package dev.skymansandy.wiretap.data.db.driver

import app.cash.sqldelight.db.SqlDriver

internal expect class DriverFactory() {
    fun createDriver(): SqlDriver
}
