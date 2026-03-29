/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.db.room

import androidx.room.Room
import androidx.room.RoomDatabase
import dev.skymansandy.wiretap.helper.constants.DB_NAME

internal actual fun createWiretapDatabaseBuilder(): RoomDatabase.Builder<WiretapRoomDatabase> {
    return Room.databaseBuilder<WiretapRoomDatabase>(
        name = DB_NAME,
    )
}
