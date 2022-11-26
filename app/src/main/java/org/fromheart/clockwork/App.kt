package org.fromheart.clockwork

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.fromheart.clockwork.data.repository.AlarmRepository
import org.fromheart.clockwork.data.repository.StopwatchRepository
import org.fromheart.clockwork.data.repository.TimerRepository
import org.fromheart.clockwork.di.appModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {

    private val scope = CoroutineScope(SupervisorJob())

    private val alarmRepository: AlarmRepository by inject()
    private val timerRepository: TimerRepository by inject()
    private val stopwatchRepository: StopwatchRepository by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@App)
            modules(appModule)
        }

        scope.launch {
            alarmRepository.closeAlarm()

            timerRepository.resetUnfinishedTimers()

            stopwatchRepository.addDefaultStopwatch()
        }
    }
}