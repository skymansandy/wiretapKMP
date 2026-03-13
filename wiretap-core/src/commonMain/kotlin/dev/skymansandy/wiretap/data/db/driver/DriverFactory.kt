package dev.skymansandy.wiretap.data.db.driver

import app.cash.sqldelight.db.SqlDriver

expect class DriverFactory() {
    fun createDriver(): SqlDriver
}
