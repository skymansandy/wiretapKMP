package dev.skymansandy.wiretap.data.db.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import dev.skymansandy.wiretap.data.db.room.dao.HttpLogsDao
import dev.skymansandy.wiretap.data.db.room.dao.RulesDao
import dev.skymansandy.wiretap.data.db.room.dao.SocketLogsDao
import dev.skymansandy.wiretap.data.db.room.entity.HttpLogEntity
import dev.skymansandy.wiretap.data.db.room.entity.RuleEntity
import dev.skymansandy.wiretap.data.db.room.entity.SocketLogEntity
import dev.skymansandy.wiretap.data.db.room.entity.SocketMessageEntity

@Database(
    entities = [
        HttpLogEntity::class,
        RuleEntity::class,
        SocketLogEntity::class,
        SocketMessageEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(WiretapDatabaseConstructor::class)
internal abstract class WiretapRoomDatabase : RoomDatabase() {

    abstract fun httpRoomDao(): HttpLogsDao

    abstract fun ruleRoomDao(): RulesDao

    abstract fun socketRoomDao(): SocketLogsDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA")
internal expect object WiretapDatabaseConstructor : RoomDatabaseConstructor<WiretapRoomDatabase> {

    override fun initialize(): WiretapRoomDatabase
}
