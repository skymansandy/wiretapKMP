package dev.skymansandy.wiretap.data.db.room

import androidx.room.Room
import androidx.room.RoomDatabase
import dev.skymansandy.wiretap.helper.constants.DB_NAME
import platform.Foundation.NSHomeDirectory

internal actual fun createWiretapDatabaseBuilder(): RoomDatabase.Builder<WiretapRoomDatabase> {
    val dbFilePath = NSHomeDirectory() + "/Documents/$DB_NAME"
    return Room.databaseBuilder<WiretapRoomDatabase>(
        name = dbFilePath,
    )
}
