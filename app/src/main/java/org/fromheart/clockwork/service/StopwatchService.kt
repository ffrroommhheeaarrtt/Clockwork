package org.fromheart.clockwork.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.fromheart.clockwork.*
import org.fromheart.clockwork.data.model.StopwatchFlag
import org.fromheart.clockwork.data.model.StopwatchTime
import org.fromheart.clockwork.repository.StopwatchRepository
import org.fromheart.clockwork.state.StopwatchState
import org.fromheart.clockwork.ui.main.MainActivity
import kotlin.system.measureTimeMillis

class StopwatchService : Service() {

    companion object {

        val timeChannel = Channel<Long>(Channel.CONFLATED)

        private var stopwatchTime: Long = 0L

        fun getStopwatchTime(): Long = stopwatchTime
    }

    private val scope = CoroutineScope(SupervisorJob())

    private lateinit var repository: StopwatchRepository

    private var lastFlagNumber: Long? = null

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
            Intent(applicationContext, StopwatchService::class.java).setAction(ACTION_START_STOPWATCH),
            FLAG_IMMUTABLE
        )
        val pausePendingIntent = PendingIntent.getService(
            applicationContext,
            0,
            Intent(applicationContext, StopwatchService::class.java).setAction(ACTION_PAUSE_STOPWATCH),
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
            Intent(applicationContext, StopwatchService::class.java).setAction(ACTION_SET_STOPWATCH_FLAG),
            FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(applicationContext, STOPWATCH_CHANNEL_ID).run {
            setContentTitle(applicationContext.getString(R.string.menu_stopwatch))
            setSmallIcon(R.drawable.ic_stopwatch)
            if (start) {
                if (lastFlagNumber == null) setContentText(getFormattedServiceStopwatchTime(time))
                else setContentText("${getFormattedServiceStopwatchTime(time)}\n${getString(R.string.button_flag)} $lastFlagNumber")
                addAction(R.drawable.ic_pause, getString(R.string.button_pause), pausePendingIntent)
                addAction(R.drawable.ic_flag, getString(R.string.button_flag), flagPendingIntent)
            } else {
                setContentText("${getFormattedServiceStopwatchTime(time)}\n${getString(R.string.stopwatch_paused)}")
                addAction(R.drawable.ic_play, getString(R.string.button_start), startPendingIntent)
                addAction(R.drawable.ic_stop, getString(R.string.button_stop), stopPendingIntent)
            }
            setContentIntent(stopwatchPendingIntent)
            setSilent(true)
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

        repository = StopwatchRepository(application.app.database.stopwatchDao())

        scope.launch {
            var start: Boolean
            stopwatchTime = repository.getTime()
            lastFlagNumber = repository.dao.getLastFlag()?.id

            repository.dao.getStopWatchFlow().collect { stopwatch ->
                when (stopwatch.state) {
                    StopwatchState.STOPPED -> {
                        start = false
                        repository.dao.insert(StopwatchTime(time = 0L))
                        repository.dao.deleteFlags()
                        stopSelf()
                        cancel()
                    }
                    StopwatchState.PAUSED -> {
                        start = false
                        startForeground(STOPWATCH_ID, createStopwatchNotification(false, stopwatchTime))
                    }
                    StopwatchState.STARTED -> {
                        start = true
                        startForeground(STOPWATCH_ID, createStopwatchNotification(true, stopwatchTime))
                        launch {
                            while (start) {
                                measureTimeMillis {
                                    timeChannel.send(stopwatchTime)
                                    if (stopwatchTime % SECOND_IN_MILLIS <= 10L) {
                                        startForeground(STOPWATCH_ID, createStopwatchNotification(true, stopwatchTime))
                                    }
                                    delay(10L)
                                }.let { stopwatchTime += it }
                            }
                            timeChannel.send(stopwatchTime)
                            repository.dao.insert(StopwatchTime(time = stopwatchTime))
                            cancel()
                        }
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_STOPWATCH -> scope.launch {
                repository.setState(StopwatchState.STARTED)
            }
            ACTION_PAUSE_STOPWATCH -> scope.launch {
                repository.setState(StopwatchState.PAUSED)
            }
            ACTION_STOP_STOPWATCH -> scope.launch {
                repository.setState(StopwatchState.STOPPED)
            }
            ACTION_SET_STOPWATCH_FLAG -> scope.launch {
                val time = getStopwatchTime() / 10L * 10L
                repository.dao.getLastFlag().let {
                    if (it == null) {
                        lastFlagNumber = 1L
                        repository.dao.insert(StopwatchFlag(1L, time, time))
                    }
                    else {
                        lastFlagNumber = it.id + 1L
                        repository.dao.insert(StopwatchFlag(it.id + 1L, time - it.flagTime, time))
                    }
                }
                startForeground(STOPWATCH_ID, createStopwatchNotification(true, time))
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null
}