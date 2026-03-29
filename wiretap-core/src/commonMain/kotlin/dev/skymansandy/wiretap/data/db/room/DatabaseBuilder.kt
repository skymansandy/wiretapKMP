/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.data.db.room

import androidx.room.RoomDatabase

internal expect fun createWiretapDatabaseBuilder(): RoomDatabase.Builder<WiretapRoomDatabase>
