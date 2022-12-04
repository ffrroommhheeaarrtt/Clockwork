package org.fromheart.clockwork.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import kotlinx.coroutines.*
import org.fromheart.clockwork.R
import org.fromheart.clockwork.data.model.StopwatchState
import org.fromheart.clockwork.data.repository.StopwatchRepository
import org.fromheart.clockwork.ui.screen.main.MainActivity
import org.fromheart.clockwork.util.*
import org.koin.android.ext.android.inject
import kotlin.system.measureTimeMillis

class StopwatchService : Service() {

    private val scope = CoroutineScope(SupervisorJob())

    private val repository: StopwatchRepository by inject()

    private var stopwatchTime: Long = 0L

    private var lastFlagNumber: Long? = null

    private fun roundTime(time: Long): Long = time / 10L * 10L

    private fun createStopwatchNotification(start: Boolean, time: Long): Notification {
        val stopwatchPendingIntent = TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(Intent(applicationContext, MainActivity::class.java).apply {
                action = ACTION_STOPWATCH_FRAGMENT
            })
            getPendingIntent(0, FLAG_IMMUTABLE)
        }
        val startPendingIntent = PendingIntent.getService(
            applicationContext,
            0,
            Intent(applicationContext, StopwatchService::class.java).setAction(
                ACTION_START_STOPWATCH
            ),
            FLAG_IMMUTABLE
        )
        val pausePendingIntent = PendingIntent.getService(
            applicationContext,
            0,
            Intent(applicationContext, StopwatchService::class.java).setAction(
                ACTION_PAUSE_STOPWATCH
            ),
            FLAG_IMMUTABLE
        )
        val stopPendingIntent = PendingIntent.getService(
            applicationContext,
            0,
            Intent(applicationContext, StopwatchService::class.java).setAction(ACTION_STOP_STOPWATCH),
            FLAG_IMMUTABLE
        )
        val flagPendingIntent = PendingIntent.getService(
            applicationContext,
            0,
            Intent(applicationContext, StopwatchService::class.java).setAction(
                ACTION_SET_STOPWATCH_FLAG
            ),
            FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(applicationContext, STOPWATCH_CHANNEL_ID).run {
            setContentTitle(applicationContext.getString(R.string.menu_stopwatch))
            setSmallIcon(R.drawable.ic_stopwatch)
            if (start) {
                if (lastFlagNumber == null) setContentText(formatStopwatchServiceTime(time))
                else setContentText("${formatStopwatchServiceTime(time)}\n${getString(R.string.button_flag)} $lastFlagNumber")
                addAction(R.drawable.ic_pause, getString(R.string.button_pause), pausePendingIntent)
                addAction(R.drawable.ic_flag, getString(R.string.button_flag), flagPendingIntent)
            } else {
                setContentText("${formatStopwatchServiceTime(time)}\n${getString(R.string.stopwatch_paused)}")
                addAction(R.drawable.ic_play, getString(R.string.button_start), startPendingIntent)
                addAction(R.drawable.ic_stop, getString(R.string.button_stop), stopPendingIntent)
            }
            setContentIntent(stopwatchPendingIntent)
            setSilent(true)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setShowWhen(false)
            setOngoing(true)
            foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
            build()
        }
    }

    override fun onCreate() {
        super.onCreate()

        scope.launch {
            var stopwatchJob: Job? = null
            stopwatchTime = repository.getPauseTime()
            lastFlagNumber = repository.getLastFlagId()

            repository.stopwatchState.collect { state ->
                when (state) {
                    StopwatchState.STARTED -> {
                        startForeground(STOPWATCH_ID, createStopwatchNotification(true, stopwatchTime))
                        stopwatchJob = launch {
                            var remainder = stopwatchTime % SECOND_IN_MILLIS
                            while (true) {
                                measureTimeMillis {
                                    if (remainder > stopwatchTime % SECOND_IN_MILLIS) {
                                        startForeground(STOPWATCH_ID, createStopwatchNotification(true, stopwatchTime))
                                    }
                                    remainder = stopwatchTime % SECOND_IN_MILLIS
                                    repository.timeChannel.send(stopwatchTime)
                                    delay(10L)
                                }.let { stopwatchTime += it }
                            }
                        }
                    }
                    StopwatchState.PAUSED -> {
                        stopwatchJob?.cancelAndJoin()
                        stopwatchJob = launch {
                            startForeground(STOPWATCH_ID, createStopwatchNotification(false, stopwatchTime))
                            repository.timeChannel.send(stopwatchTime)
                            repository.setTime(stopwatchTime)
                        }
                    }
                    StopwatchState.STOPPED -> {
                        stopSelf()
                        stopwatchJob?.cancelAndJoin()
                        repository.timeChannel.send(0L)
                        repository.resetRunningStopwatch()
                        cancel()
                        return@collect
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            when (intent?.action) {
                ACTION_START_STOPWATCH -> repository.setState(StopwatchState.STARTED)
                ACTION_PAUSE_STOPWATCH -> repository.setState(StopwatchState.PAUSED)
                ACTION_STOP_STOPWATCH -> repository.setState(StopwatchState.STOPPED)
                ACTION_SET_STOPWATCH_FLAG -> {
                    val time = roundTime(stopwatchTime)
                    lastFlagNumber = repository.setFlag(time)
                    startForeground(STOPWATCH_ID, createStopwatchNotification(true, time))
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null
}