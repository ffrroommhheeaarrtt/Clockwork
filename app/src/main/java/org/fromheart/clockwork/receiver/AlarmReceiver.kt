package org.fromheart.clockwork.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import org.fromheart.clockwork.ACTION_FINISH_ALARM_ACTIVITY
import org.fromheart.clockwork.ACTION_STOP_ALARM
import org.fromheart.clockwork.data.AppDatabase
import org.fromheart.clockwork.repository.AlarmRepository
import org.fromheart.clockwork.service.AlarmService
import kotlin.math.abs

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val RESTART_MESSAGE = "restart"

        internal var alarmJob: Job? = null
    }

    private fun getServiceIntent(context: Context): Intent = Intent(context, AlarmService::class.java)

    private fun stopAlarm(context: Context) = LocalBroadcastManager.getInstance(context).run {
        sendBroadcast(Intent(ACTION_FINISH_ALARM_ACTIVITY))
        context.stopService(getServiceIntent(context))
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_STOP_ALARM -> alarmJob?.cancel()
            else -> CoroutineScope(Job()).launch {
                val alarmRepository = AlarmRepository(AppDatabase.getDatabase(context).alarmDao())
                if (abs(System.currentTimeMillis() - alarmRepository.alarmDao.getNextAlarms().first().time) < 60000L) {
                    alarmJob?.cancel(RESTART_MESSAGE)
                    ContextCompat.startForegroundService(context, getServiceIntent(context))
                    alarmJob = launch {
                        try {
                            delay(90000L)
                            stopAlarm(context)
                        } catch (e: CancellationException) {
                            if (e.message != RESTART_MESSAGE) stopAlarm(context)
                        }
                    }
                }
            }
        }
    }
}