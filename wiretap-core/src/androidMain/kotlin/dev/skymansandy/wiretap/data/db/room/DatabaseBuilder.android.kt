package dev.skymansandy.wiretap.data.db.room

import androidx.room.Room
import androidx.room.RoomDatabase
import dev.skymansandy.wiretap.helper.constants.DB_NAME
import dev.skymansandy.wiretap.helper.initializer.WiretapContextProvider

internal actual fun createWiretapDatabaseBuilder(): RoomDatabase.Builder<WiretapRoomDatabase> {
    val context = WiretapContextProvider.context
    val dbFile = context.getDatabasePath(DB_NAME)
    return Room.databaseBuilder<WiretapRoomDatabase>(
        context = context,
        name = dbFile.absolutePath,
    )
}
