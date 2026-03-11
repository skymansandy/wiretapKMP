package dev.skymansandy.wiretap.db

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory() {
    fun createDriver(): SqlDriver
}
