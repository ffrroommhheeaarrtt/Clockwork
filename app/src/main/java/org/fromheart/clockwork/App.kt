package org.fromheart.clockwork

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.fromheart.clockwork.data.AppDatabase
import org.fromheart.clockwork.data.repository.AlarmRepository
import org.fromheart.clockwork.data.repository.StopwatchRepository
import org.fromheart.clockwork.data.repository.TimerRepository

class App : Application() {

    private val scope = CoroutineScope(SupervisorJob())

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val alarmRepository: AlarmRepository by lazy { AlarmRepository.getInstance(database.alarmDao()) }
    val stopwatchRepository: StopwatchRepository by lazy { StopwatchRepository.getInstance(database.stopwatchDao()) }
    val timerRepository: TimerRepository by lazy { TimerRepository.getInstance(database.timerDao()) }

    override fun onCreate() {
        super.onCreate()

        scope.launch {
            alarmRepository.closeAlarm()

            timerRepository.resetUnfinishedTimers()

            stopwatchRepository.addDefaultStopwatch()
        }
    }
}