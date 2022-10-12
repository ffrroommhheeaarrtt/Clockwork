package org.fromheart.clockwork.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import org.fromheart.clockwork.*
import org.fromheart.clockwork.receiver.AlarmReceiver
import org.fromheart.clockwork.repository.AlarmRepository
import org.fromheart.clockwork.ui.alarm.AlarmActivity
import java.util.Calendar
import kotlin.math.abs

class AlarmService : Service() {

    companion object {
        private const val CANCEL_MESSAGE = "cancel"

        private var alarmJob: Job? = null
        private var snoozedAlarmJob: Job? = null

        fun stop() = alarmJob?.cancel()
        fun dismiss() = snoozedAlarmJob?.cancel()
    }

    private fun createAlarmNotification(): Notification {
        val alarmPendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, AlarmActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            FLAG_IMMUTABLE
        )
        val snoozePendingIntent  = PendingIntent.getBroadcast(
            applicationContext,
            0,
            Intent(applicationContext, AlarmReceiver::class.java).setAction(ACTION_SNOOZE_ALARM),
            FLAG_IMMUTABLE
        )
        val stopPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            Intent(applicationContext, AlarmReceiver::class.java).setAction(ACTION_STOP_ALARM),
            FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(applicationContext, ALARM_CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.menu_alarm))
            .setContentText(time)
            .setSmallIcon(R.drawable.ic_alarm)
            .addAction(R.drawable.ic_snooze, applicationContext.getString(R.string.button_snooze), snoozePendingIntent)
            .addAction(R.drawable.ic_stop, applicationContext.getString(R.string.button_stop), stopPendingIntent)
            .setContentIntent(alarmPendingIntent)
            .setFullScreenIntent(alarmPendingIntent, true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setChannelId(ALARM_CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createSnoozeAlarmNotification(): Notification {
        val dismissPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            Intent(applicationContext, AlarmReceiver::class.java).setAction(ACTION_DISMISS_ALARM),
            FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(applicationContext, ALARM_CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.title_snoozed_alarm))
            .setContentText(Calendar.getInstance().apply { add(Calendar.MINUTE, 2) }
                .let { getTime(it[Calendar.HOUR_OF_DAY], it[Calendar.MINUTE]) })
            .setSmallIcon(R.drawable.ic_alarm)
            .addAction(R.drawable.ic_dismiss_alarm, applicationContext.getString(R.string.button_dismiss), dismissPendingIntent)
            .setSilent(true)
            .setChannelId(ALARM_CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setOngoing(true)
            .build()
    }

    private fun stopAlarm() = LocalBroadcastManager.getInstance(applicationContext).run {
        sendBroadcast(Intent(ACTION_FINISH_ALARM_ACTIVITY))
        stopSelf()
    }

    private suspend fun startAlarm() = coroutineScope {
        alarmJob?.cancel(CANCEL_MESSAGE)
        startForeground(ALARM_ID, createAlarmNotification())
        alarmJob = launch {
            try {
                delay(90000L)
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, AlarmService::class.java).setAction(ACTION_SNOOZE_ALARM)
                )
            } catch (e: CancellationException) {
                if (e.message != CANCEL_MESSAGE) stopAlarm()
            }
        }
    }

    private suspend fun startSnoozedAlarm() = coroutineScope {
        alarmJob?.cancel(CANCEL_MESSAGE)
        startForeground(SNOOZED_ALARM_ID, createSnoozeAlarmNotification())
        snoozedAlarmJob = launch {
            try {
                delay(120000L)
                startAlarm()
            } catch (e: CancellationException) {
                if (e.message != CANCEL_MESSAGE) stopSelf()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Job()).launch {
            when (intent?.action) {
                ACTION_SNOOZE_ALARM -> {
                    startSnoozedAlarm()
                }
                else -> {
                    val repository = AlarmRepository(application.app.database.alarmDao())
                    if (abs(System.currentTimeMillis() - repository.alarmDao.getNextAlarms().first().time) < 60000L) {
                        repository.setNextAlarm(applicationContext)
                        snoozedAlarmJob?.cancel(CANCEL_MESSAGE)
                        startAlarm()
                    } else if (alarmJob == null && snoozedAlarmJob == null) stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null
}
