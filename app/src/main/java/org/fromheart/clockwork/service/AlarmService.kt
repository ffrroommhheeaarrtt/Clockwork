package org.fromheart.clockwork.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import org.fromheart.clockwork.R
import org.fromheart.clockwork.data.repository.AlarmRepository
import org.fromheart.clockwork.ui.screen.alarm.AlarmActivity
import org.fromheart.clockwork.util.*
import org.koin.android.ext.android.inject
import java.util.*

private const val ALARM_DURATION = 90000L
private const val SLEEP_DURATION = 60000L
private const val SLEEP_DURATION_IN_MINUTES = (SLEEP_DURATION / MINUTE_IN_MILLIS).toInt()
private const val MESSAGE_RESTART = "restart"

class AlarmService : Service() {

    private val scope = CoroutineScope(SupervisorJob())

    private val repository: AlarmRepository by inject()

    private var alarmJob: Job? = null

    private fun createAlarmNotification(): Notification {
        val alarmPendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            FLAG_IMMUTABLE
        )
        val snoozePendingIntent  = PendingIntent.getService(
            applicationContext,
            0,
            Intent(applicationContext, AlarmService::class.java).setAction(ACTION_SNOOZE_ALARM),
            FLAG_IMMUTABLE
        )
        val stopPendingIntent = PendingIntent.getService(
            applicationContext,
            0,
            Intent(applicationContext, AlarmService::class.java).setAction(ACTION_STOP_ALARM),
            FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(applicationContext, ALARM_CHANNEL_ID).run {
            setContentTitle(applicationContext.getString(R.string.menu_alarm))
            setContentText(formattedCurrentTime)
            setSmallIcon(R.drawable.ic_alarm)
            addAction(R.drawable.ic_snooze, applicationContext.getString(R.string.button_snooze), snoozePendingIntent)
            addAction(R.drawable.ic_stop, applicationContext.getString(R.string.button_stop), stopPendingIntent)
            setContentIntent(alarmPendingIntent)
            setFullScreenIntent(alarmPendingIntent, true)
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

    private fun createSnoozeAlarmNotification(): Notification {
        val dismissPendingIntent = PendingIntent.getService(
            applicationContext,
            0,
            Intent(applicationContext, AlarmService::class.java).setAction(ACTION_STOP_ALARM),
            FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(applicationContext, ALARM_CHANNEL_ID).run {
            setContentTitle(applicationContext.getString(R.string.title_snoozed_alarm))
            setContentText(Calendar.getInstance().apply { add(Calendar.MINUTE, SLEEP_DURATION_IN_MINUTES) }.let {
                getFormattedTime(it[Calendar.HOUR_OF_DAY], it[Calendar.MINUTE])
            })
            setSmallIcon(R.drawable.ic_alarm)
            addAction(R.drawable.ic_dismiss_alarm, applicationContext.getString(R.string.button_dismiss), dismissPendingIntent)
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

    private suspend fun startAlarm() = coroutineScope {
        startForeground(ALARM_ID, createAlarmNotification())
        delay(ALARM_DURATION)
    }

    private suspend fun startSnoozeAlarm() = coroutineScope {
        finishAlarmActivity()
        startForeground(SNOOZED_ALARM_ID, createSnoozeAlarmNotification())
        delay(SLEEP_DURATION)
    }

    private fun finishAlarmActivity() = LocalBroadcastManager.getInstance(applicationContext).run {
        sendBroadcast(Intent(ACTION_FINISH_ALARM_ACTIVITY))
    }

    private fun stopAlarm() {
        finishAlarmActivity()
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SNOOZE_ALARM -> {
                alarmJob?.cancel(MESSAGE_RESTART)
                alarmJob = scope.launch {
                    try {
                        while (isActive) {
                            startSnoozeAlarm()
                            startAlarm()
                        }
                    } catch (e: CancellationException) {
                        if (e.message != MESSAGE_RESTART) stopAlarm()
                    }
                }
            }
            ACTION_STOP_ALARM -> alarmJob?.cancel()
            else -> {
                alarmJob?.cancel(MESSAGE_RESTART)
                scope.launch { repository.setNextAlarm(applicationContext) }
                alarmJob = scope.launch {
                    try {
                        while (isActive) {
                            startAlarm()
                            startSnoozeAlarm()
                        }
                    } catch (e: CancellationException) {
                        if (e.message != MESSAGE_RESTART) stopAlarm()
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null
}
