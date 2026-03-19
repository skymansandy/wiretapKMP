package dev.skymansandy.wiretap.data.db.driver

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import dev.skymansandy.wiretap.db.WiretapDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

internal actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val schema = WiretapDatabase.Companion.Schema
        return try {
            NativeSqliteDriver(schema, "wiretap.db")
        } catch (_: Exception) {
            deleteDatabase()
            NativeSqliteDriver(schema, "wiretap.db")
        }
    }

    @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
    private fun deleteDatabase() {
        val fileManager = NSFileManager.defaultManager
        val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        val docUrl = urls.firstOrNull() ?: return
        @Suppress("CAST_NEVER_SUCCEEDS")
        val dbUrl = (docUrl as platform.Foundation.NSURL).URLByAppendingPathComponent("wiretap.db")
        dbUrl?.path?.let { fileManager.removeItemAtPath(it, null) }
    }
}
