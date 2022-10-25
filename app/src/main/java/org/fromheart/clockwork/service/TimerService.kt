package org.fromheart.clockwork.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import org.fromheart.clockwork.*
import org.fromheart.clockwork.data.model.TimerStatus
import org.fromheart.clockwork.repository.TimerRepository
import org.fromheart.clockwork.ui.main.MainActivity
import org.fromheart.clockwork.ui.timer.TimerActivity
import kotlin.system.measureTimeMillis

class TimerService : Service() {

    companion object {

        private const val ALERT_TIMER_DURATION = 60000L

        private const val MESSAGE_PAUSE = "pause"
        private var timerJob: Job? = null

        fun pause() = timerJob?.cancel(MESSAGE_PAUSE)
        fun stop() = timerJob?.cancel()
    }

    private lateinit var localBroadcastManager: LocalBroadcastManager

    private lateinit var repository: TimerRepository

    private fun createTimerNotification(time: String): Notification {
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

        return NotificationCompat.Builder(applicationContext, TIMER_CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.menu_timer))
            .setContentText(time)
            .setSmallIcon(R.drawable.ic_timer)
            .addAction(R.drawable.ic_pause, getString(R.string.button_pause), pausePendingIntent)
            .addAction(R.drawable.ic_stop, getString(R.string.button_stop), stopPendingIntent)
            .setContentIntent(timerPendingIntent)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createAlertTimerNotification(time: String): Notification {
        val alertTimerPendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, TimerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                data = Uri.parse(time)
            },
            FLAG_IMMUTABLE
        )
        val stopPendingIntent = PendingIntent.getService(
            applicationContext,
            0,
            Intent(applicationContext, TimerService::class.java).setAction(ACTION_STOP_TIMER),
            FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(applicationContext, TIMER_CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.menu_timer))
            .setContentText(time)
            .setSmallIcon(R.drawable.ic_timer)
            .addAction(R.drawable.ic_stop, getString(R.string.button_stop), stopPendingIntent)
            .setContentIntent(alertTimerPendingIntent)
            .setFullScreenIntent(alertTimerPendingIntent, true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createMissedTimerNotification(alertTime: String): Notification {
        return NotificationCompat.Builder(applicationContext, TIMER_CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.missed_timer))
            .setContentText(alertTime)
            .setSmallIcon(R.drawable.ic_timer)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .build()
    }

    private fun finishTimerActivity() = localBroadcastManager.sendBroadcast(Intent(ACTION_FINISH_TIMER_ACTIVITY))

    private fun stopTimer() {
        finishTimerActivity()
        stopSelf()
    }

    override fun onCreate() {
        super.onCreate()

        localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)

        repository = TimerRepository(application.app.database.timerDao())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Job()).launch {
            var timer = repository.timerDao.getRunningTimer()!!
            var time = timer.time

            when (intent?.action) {
                ACTION_PAUSE_TIMER -> {
                    repository.timerDao.update(
                        timer.copy(
                            status = TimerStatus.PAUSE.number,
                            time = time
                        )
                    )
                    timerJob?.cancel()
                }
                ACTION_STOP_TIMER -> {
                    repository.timerDao.update(
                        timer.copy(
                            status = TimerStatus.STOP.number,
                            time = getTimerTime(timer.hour, timer.minute, timer.second)
                        )
                    )
                    timerJob?.cancel()
                }
                else -> {
                    startForeground(TIMER_ID, createTimerNotification(getFormattedTime(timer.hour, timer.minute, timer.second)))
                    timerJob = launch {
                        try {
                            while (time > 0L) {
                                val systemTime = measureTimeMillis {
                                    delay(10L)
                                    if (timer.time - time >= 500L) {
                                        startForeground(TIMER_ID, createTimerNotification(getFormattedTimerTime(time)))
                                        repository.timerDao.update(timer.copy(time = time))
                                        timer = repository.timerDao.getRunningTimer()!!
                                    }
                                }
                                time -= systemTime
                            }

                            val alertTime = currentTime
                            startForeground(
                                ALERT_TIMER_ID,
                                createAlertTimerNotification(getFormattedTime(timer.hour, timer.minute, timer.second))
                            )
                            delay(ALERT_TIMER_DURATION)
                            repository.timerDao.update(
                                timer.copy(
                                    status = TimerStatus.STOP.number,
                                    time = getTimerTime(timer.hour, timer.minute, timer.second)
                                )
                            )
                            NotificationManagerCompat.from(applicationContext).apply {
                                notify(MISSED_TIMER_ID, createMissedTimerNotification(alertTime))
                            }
                            stopTimer()
                        } catch (e: CancellationException) {
                            stopTimer()
                            if (e.message == MESSAGE_PAUSE) repository.timerDao.update(timer.copy(time = time))
                        }
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null
}