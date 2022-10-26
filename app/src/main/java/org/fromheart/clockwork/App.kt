package org.fromheart.clockwork

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.fromheart.clockwork.data.AppDatabase
import org.fromheart.clockwork.data.model.TimerStatus
import org.fromheart.clockwork.repository.AlarmRepository
import org.fromheart.clockwork.repository.TimerRepository

class App : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob()).launch {
            val alarmRepository = AlarmRepository(database.alarmDao())
            val timerRepository = TimerRepository(database.timerDao())

            alarmRepository.alarmDao.getOpenAlarm()?.let { alarm ->
                alarmRepository.alarmDao.update(alarm.copy(isOpened = false))
            }

            timerRepository.timerDao.getRunningTimer()?.let { timer ->
                timerRepository.timerDao.update(timer.copy(status = TimerStatus.PAUSE.number))
            }
        }
    }
}