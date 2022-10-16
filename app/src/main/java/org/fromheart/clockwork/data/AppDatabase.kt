package org.fromheart.clockwork.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.fromheart.clockwork.data.converter.AlarmConverter
import org.fromheart.clockwork.data.dao.AlarmDao
import org.fromheart.clockwork.data.dao.TimerDao
import org.fromheart.clockwork.data.model.Alarm
import org.fromheart.clockwork.data.model.Timer

@Database(entities = [Alarm::class, Timer::class], version = 1, exportSchema = false)
@TypeConverters(AlarmConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao
    abstract fun timerDao(): TimerDao

    companion object {
        @Volatile
        private lateinit var instance: AppDatabase

        fun getDatabase(context: Context): AppDatabase {
            synchronized(AppDatabase::class) {
                if (!::instance.isInitialized) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "clockwork_database"
                    ).fallbackToDestructiveMigration().build()
                }
            }
            return instance
        }
    }
}

