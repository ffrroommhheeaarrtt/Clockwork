package org.fromheart.clockwork.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.fromheart.clockwork.*
import org.fromheart.clockwork.receiver.AlarmReceiver
import org.fromheart.clockwork.repository.AlarmRepository
import org.fromheart.clockwork.ui.alarm.AlarmActivity

class AlarmService : Service() {

    private fun createNotification(): Notification {
        val alarmPendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, AlarmActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
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
            .addAction(R.drawable.ic_stop, applicationContext.getString(R.string.button_stop), stopPendingIntent)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(alarmPendingIntent)
            .setFullScreenIntent(alarmPendingIntent, true)
            .setShowWhen(false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        CoroutineScope(Job()).launch {
            AlarmRepository(application.app.database.alarmDao()).setNextAlarm(applicationContext)
        }
        startForeground(ALARM_ID, createNotification())
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null
}
