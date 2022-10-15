package org.fromheart.clockwork.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.fromheart.clockwork.*
import org.fromheart.clockwork.service.AlarmService
import kotlin.math.abs

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            ACTION_SNOOZE_ALARM -> ContextCompat.startForegroundService(
                context,
                Intent(context, AlarmService::class.java).setAction(ACTION_SNOOZE_ALARM)
            )
            ACTION_STOP_ALARM -> AlarmService.stop()
            else -> {
                CoroutineScope(Job()).launch {
                    val nextAlarm = context.dataStore.data.first()[longPreferencesKey(PREFERENCES_KEY_ALARM_TIME)]
                    if (nextAlarm != null && abs(System.currentTimeMillis() - nextAlarm) < 60000L) {
                        ContextCompat.startForegroundService(context, Intent(context, AlarmService::class.java))
                    }
                }
            }
        }
    }
}