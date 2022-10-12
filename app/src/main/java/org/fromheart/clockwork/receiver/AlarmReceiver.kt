package org.fromheart.clockwork.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import org.fromheart.clockwork.ACTION_DISMISS_ALARM
import org.fromheart.clockwork.ACTION_SNOOZE_ALARM
import org.fromheart.clockwork.ACTION_STOP_ALARM
import org.fromheart.clockwork.service.AlarmService
import org.fromheart.clockwork.snoozeAlarmService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SNOOZE_ALARM -> context.snoozeAlarmService()
            ACTION_STOP_ALARM -> AlarmService.stop()
            ACTION_DISMISS_ALARM -> AlarmService.dismiss()
            else -> ContextCompat.startForegroundService(context, Intent(context, AlarmService::class.java))
        }
    }
}