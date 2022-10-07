package org.fromheart.clockwork

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.fromheart.clockwork.data.AppDatabase
import org.fromheart.clockwork.repository.AlarmRepository

class App : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Job()).launch {
            val alarmRepository = AlarmRepository(database.alarmDao())
            alarmRepository.alarmDao.getOpenAlarm()?.let { alarm ->
                alarmRepository.alarmDao.update(alarm.copy(visibility = false))
            }
        }
    }
}