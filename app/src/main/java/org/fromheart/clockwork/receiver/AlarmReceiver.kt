package org.fromheart.clockwork.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.fromheart.clockwork.*
import org.fromheart.clockwork.service.AlarmService
import kotlin.math.abs

class AlarmReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob())

    override fun onReceive(context: Context, intent: Intent?) {
        scope.launch {
            val nextAlarm = context.dataStore.data.first()[longPreferencesKey(PREFERENCES_KEY_ALARM_TIME)]
            if (nextAlarm != null && abs(System.currentTimeMillis() - nextAlarm) < MINUTE_IN_MILLIS) {
                ContextCompat.startForegroundService(context, Intent(context, AlarmService::class.java))
            }
        }
    }
}