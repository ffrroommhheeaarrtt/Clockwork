package org.fromheart.clockwork.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.flow.receiveAsFlow
import org.fromheart.clockwork.R
import org.fromheart.clockwork.data.model.TimerState
import org.fromheart.clockwork.data.repository.TimerRepository
import org.fromheart.clockwork.ui.screen.main.MainActivity
import org.fromheart.clockwork.ui.screen.timer.TimerActivity
import org.fromheart.clockwork.util.*
import org.koin.android.ext.android.inject
import kotlin.collections.set
import kotlin.system.measureTimeMillis

class TimerService : Service() {

    private val scope = CoroutineScope(SupervisorJob())

    private val repository: TimerRepository by inject()

    private lateinit var localBroadcastManager: LocalBroadcastManager

    private val alertTimerDuration = 2.minutesToMillis()

    private val timeMap = mutableMapOf<Long, Long>()

    private var alertTimerJob: Job? = null

    private fun Map<Long, Long>.keyWithMinValue(): Long {
        var minKey = 0L
        var minValue = Long.MAX_VALUE
        forEach {
            if (it.value < minValue) {
                minKey = it.key
                minValue = it.value
            }
        }
        return minKey
    }


    private fun createTimerNotification(time: Long): Notification {
        val timerPendingIntent = TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(Intent(applicationContext, MainActivity::class.java).apply {
                action = ACTION_TIMER_FRAGMENT
            })
            getPendingIntent(0, FLAG_IMMUTABLE)
        }
        val pausePendingIntent = PendingIntent.getService(
            applicationContext,
            0,
            Intent(applicationContext, TimerService::class.java).setAction(ACTION_PAUSE_TIMER),
            FLAG_IMMUTABLE
        )
        val stopPendingIntent = PendingIntent.getService(
            applicationContext,
            0,
            Intent(applicationContext, TimerService::class.java).setAction(ACTION_STOP_TIMER),
            FLAG_IMMUTABLE
        )
        val resetAllPendingIntent = PendingIntent.getService(
            applicationContext,
            0,
            Intent(applicationContext, TimerService::class.java).setAction(ACTION_RESET_ALL_TIMERS),
            FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(applicationContext, TIMER_CHANNEL_ID).run {
            setContentTitle(applicationContext.getString(R.string.menu_timer))
            if (timeMap.size > 1) {
                setContentText("${formatTimerItemTime(time)}\n${getString(R.string.running_timer_count)} ${timeMap.size}")
            } else setContentText(formatTimerItemTime(time))
            setSmallIcon(R.drawable.ic_timer)
            addAction(R.drawable.ic_pause, getString(R.string.button_pause), pausePendingIntent)
            addAction(R.drawable.ic_stop, getString(R.string.button_stop), stopPendingIntent)
            if (timeMap.size > 1) addAction(R.drawable.ic_reset, getString(R.string.button_reset_all), resetAllPendingIntent)
            setContentIntent(timerPendingIntent)
            setSilent(true)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setCategory(NotificationCompat.CATEGORY_ALARM)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setShowWhen(false)
            setOngoing(true)
            foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
            build()
        }
    }

    private fun createAlertTimerNotification(time: Long): Notification {
        val alertTimerPendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, TimerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            FLAG_IMMUTABLE
        )
        val stopPendingIntent = PendingIntent.getService(
            applicationContext,
            0,
            Intent(applicationContext, TimerService::class.java).setAction(ACTION_STOP_ALERT_TIMER),
            FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(applicationContext, TIMER_CHANNEL_ID).run {
            setContentTitle(applicationContext.getString(R.string.menu_timer))
            setContentText(formatTimerItemTime(time))
            setSmallIcon(R.drawable.ic_timer)
            addAction(R.drawable.ic_stop, getString(R.string.button_stop), stopPendingIntent)
            setContentIntent(alertTimerPendingIntent)
            setFullScreenIntent(alertTimerPendingIntent, true)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            priority = NotificationCompat.PRIORITY_HIGH
            setCategory(NotificationCompat.CATEGORY_ALARM)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setShowWhen(false)
            setOngoing(true)
            setOnlyAlertOnce(true)
            foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
            build()
        }
    }

    private fun createMissedTimerNotification(time: Long): Notification {
        val timerPendingIntent = TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(Intent(applicationContext, MainActivity::class.java).apply {
                action = ACTION_TIMER_FRAGMENT
            })
            getPendingIntent(0, FLAG_IMMUTABLE)
        }

        return NotificationCompat.Builder(applicationContext, TIMER_CHANNEL_ID).run {
            setContentTitle(applicationContext.getString(R.string.missed_timer))
            setContentText(formatTimerItemTime(time))
            setSmallIcon(R.drawable.ic_timer)
            setContentIntent(timerPendingIntent)
            setSilent(true)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setCategory(NotificationCompat.CATEGORY_ALARM)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setShowWhen(false)
            setAutoCancel(true)
            build()
        }
    }

    private fun finishTimerActivity() = localBroadcastManager.sendBroadcast(Intent(ACTION_FINISH_TIMER_ACTIVITY))

    override fun onCreate() {
        super.onCreate()

        localBroadcastManager = LocalBroadcastManager.getInstance(this)

        scope.launch {
            repository.runningTimersChannel.receiveAsFlow().collect { timer ->
                when (timer.state) {
                    TimerState.STARTED -> launch {
                        repository.updateTimer(timer)
                        repository.timerChannelMap[timer.id] = Channel(Channel.CONFLATED)
                        var time = timer.currentTime
                        timeMap[timer.id] = time
                        var remainder = time % SECOND_IN_MILLIS
                        if (timeMap.values.min() == time) {
                            startForeground(TIMER_ID, createTimerNotification(time))
                        }

                        try {
                            while (time > 0L) {
                                measureTimeMillis {
                                    timeMap[timer.id] = time
                                    repository.timerChannelMap[timer.id]?.send(time)
                                    if (remainder < time % SECOND_IN_MILLIS && timeMap.values.min() == time) {
                                        startForeground(TIMER_ID, createTimerNotification(time))
                                    }
                                    remainder = time % SECOND_IN_MILLIS
                                    delay(10L)
                                }.let { time -= it }
                            }
                            repository.updateTimer(timer.copy(state = TimerState.STOPPED, currentTime = timer.time))
                            repository.setAlertTimerTime(timer.time)
                            notificationManager.notify(ALERT_TIMER_ID, createAlertTimerNotification(timer.time))

                            alertTimerJob?.cancel()
                            alertTimerJob = launch {
                                delay(alertTimerDuration)
                                notificationManager.cancel(ALERT_TIMER_ID)
                                finishTimerActivity()
                                notificationManager.notify(MISSED_TIMER_ID, createMissedTimerNotification(timer.time))
                            }
                        } catch (_: ClosedSendChannelException) {
                        } finally {
                            timeMap.remove(timer.id)
                            repository.timerChannelMap.remove(timer.id)
                            if (repository.timerChannelMap.isEmpty()) stopForeground()
                        }
                    }
                    TimerState.PAUSED -> {
                        repository.timerChannelMap[timer.id]?.close()
                        timeMap[timer.id]?.let { repository.updateTimer(timer.copy(currentTime = it)) }
                    }
                    TimerState.STOPPED -> {
                        repository.timerChannelMap[timer.id]?.close()
                        repository.updateTimer(timer.copy(currentTime = timer.time))
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            scope.launch {
                when (action) {
                    ACTION_PAUSE_TIMER -> repository.pauseTimer(timeMap.keyWithMinValue())
                    ACTION_STOP_TIMER -> repository.stopTimer(timeMap.keyWithMinValue())
                    ACTION_RESET_ALL_TIMERS -> repository.resetRunningTimers()
                    ACTION_STOP_ALERT_TIMER -> {
                        alertTimerJob?.cancel()
                        notificationManager.cancel(ALERT_TIMER_ID)
                        finishTimerActivity()
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null
}