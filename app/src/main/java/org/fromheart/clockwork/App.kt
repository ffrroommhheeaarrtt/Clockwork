package org.fromheart.clockwork

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.fromheart.clockwork.data.AppDatabase
import org.fromheart.clockwork.data.model.Stopwatch
import org.fromheart.clockwork.data.model.StopwatchTime
import org.fromheart.clockwork.data.model.TimerStatus
import org.fromheart.clockwork.repository.AlarmRepository
import org.fromheart.clockwork.repository.StopwatchRepository
import org.fromheart.clockwork.repository.TimerRepository
import org.fromheart.clockwork.state.StopwatchState

class App : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob()).launch {
            val alarmRepository = AlarmRepository(database.alarmDao())
            val timerRepository = TimerRepository(database.timerDao())
            val stopwatchRepository = StopwatchRepository(database.stopwatchDao())

            alarmRepository.alarmDao.getOpenAlarm()?.let {
                alarmRepository.alarmDao.update(it.copy(isOpened = false))
            }

            timerRepository.timerDao.getRunningTimer()?.let {
                timerRepository.timerDao.update(it.copy(status = TimerStatus.PAUSE.number))
            }

            stopwatchRepository.dao.apply {
                if (isEmptyStopwatch() || getStopwatch().state == StopwatchState.STARTED) {
                    insert(Stopwatch())
                    insert(StopwatchTime())
                    deleteFlags()
                }
            }
        }
    }
}