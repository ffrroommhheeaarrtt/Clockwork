package org.fromheart.clockwork.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.fromheart.clockwork.data.converter.AlarmConverter
import org.fromheart.clockwork.data.converter.StopwatchConverter
import org.fromheart.clockwork.data.converter.TimerConverter
import org.fromheart.clockwork.data.dao.AlarmDao
import org.fromheart.clockwork.data.dao.StopwatchDao
import org.fromheart.clockwork.data.dao.TimeZoneDao
import org.fromheart.clockwork.data.dao.TimerDao
import org.fromheart.clockwork.data.model.*

private const val DATABASE_NAME = "clockwork_database"

@Database(
    entities = [AlarmEntity::class, TimerEntity::class, StopwatchEntity::class, StopwatchFlagEntity::class, TimeZoneEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(AlarmConverter::class, TimerConverter::class, StopwatchConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao
    abstract fun timerDao(): TimerDao
    abstract fun stopwatchDao(): StopwatchDao
    abstract fun timeZoneDao(): TimeZoneDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).run {
                    fallbackToDestructiveMigration()
                    build()
                }
            }.also { instance = it }
        }
    }
}

