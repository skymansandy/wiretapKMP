package dev.skymansandy.wiretap.data.db.room

import androidx.room.RoomDatabase

internal expect fun createWiretapDatabaseBuilder(): RoomDatabase.Builder<WiretapRoomDatabase>
